plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.15.0"
    `kotlin-dsl`
    groovy

}

group = "com.estivensh4"
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

val publishingPlugin = "jacocoCoveragePlugin"


gradlePlugin {
    plugins {
        create(publishingPlugin) {
            id = "com.estiven.jacoco-coverage"
            implementationClass = "com.estiven.jacoco.JacocoCoveragePlugin"
        }

    }
}

pluginBundle {
    // These settings are set for the whole plugin bundle
    website = "https://github.com/estivensh4/kotlin-libs-publisher"
    vcsUrl = website

    (plugins) {
        publishingPlugin {
            displayName = "Kotlin libs publisher plugin"
            description = displayName
            tags = listOf("kotlin", "publishing")
        }
    }
}
