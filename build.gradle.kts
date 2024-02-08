plugins {
    id("io.papermc.paperweight.userdev") version "1.5.10" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("xyz.jpenilla.run-paper") version "2.2.2" apply false
}

allprojects {
    apply(from = "${rootProject.projectDir}/scripts/gradle/common.gradle")
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}