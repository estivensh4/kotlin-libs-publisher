plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.15.0"
    `kotlin-dsl`
    groovy
    `maven-publish`

}

group = "io.github.estivensh4"
version = "0.0.1"

fun detectVersion(): String {
    val buildNumber = rootProject.findProperty("build.number") as String?
    return if (buildNumber != null) {
        if (hasProperty("build.number.detection")) {
            "$version-dev-$buildNumber"
        } else {
            buildNumber
        }
    } else if (hasProperty("release")) {
        version as String
    } else {
        "$version-dev"
    }
}

val detectVersionForTC by tasks.registering {
    doLast {
        println("##teamcity[buildNumber '$version']")
    }
}

val junitVersion: String by project

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")

    // For maven-publish
    implementation(gradleApi())

    // Test dependencies: kotlin-test and Junit 5
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("io.kotlintest:kotlintest-assertions:3.4.2")
    testImplementation(gradleTestKit())

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    outputs.upToDateWhen { false }
}

val jacocoCoveragePlugin = "jacocoCoveragePlugin"
val jacocoFullReportPlugin = "jacocoFullReportPlugin"


gradlePlugin {
    plugins {
        create(jacocoCoveragePlugin) {
            id = "io.github.estivensh4.jacoco-coverage"
            implementationClass = "io.github.estivensh4.jacoco.JacocoCoveragePlugin"
        }

        create(jacocoFullReportPlugin) {
            id = "io.github.estivensh4.jacoco-full-report"
            implementationClass = "io.github.estivensh4.jacoco.JacocoFullReportPlugin"
        }

    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.estivensh4"
            artifactId = "jacoco"
            version = "0.0.1"
            from(components["java"])
        }
    }
}

pluginBundle {
    website = "https://github.com/estivensh4/kotlin-libs-publisher"
    vcsUrl = website
    tags = listOf("java", "code equality", "jacoco", "code coverage", "coverage")

    (plugins) {
        jacocoCoveragePlugin {
            displayName = "Jacoco Coverage Plugin"
            description = "The io.github.estivensh4.jacoco-coverage plugin allows Gradle build scripts to configure minimum Java Code Coverage thresholds for projects, packages, classes, and files."
        }
        jacocoFullReportPlugin {
            displayName = "Jacoco Full Report Plugin"
            description = "The io.github.estivensh4.jacoco-full-report plugin adds a task that produces a Jacoco report for the combined code coverage of the tests of all subprojects of the current project."
        }
    }
}
