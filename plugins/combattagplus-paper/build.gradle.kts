plugins {
    `java-library`
    `maven-publish`
    id("net.civmc.civgradle.plugin") version "1.0.0-SNAPSHOT"
}

// Temporary hack:
// Remove the root build directory
gradle.buildFinished {
    project.buildDir.deleteRecursively()
}

allprojects {
    group = "net.civmc.combattagplus"
    version = "3.0.0-SNAPSHOT"
    description = "CombatTagPlus"
}

subprojects {
    apply(plugin = "net.civmc.civgradle.plugin")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()

        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://ci.frostcast.net/plugin/repository/everything")
        maven("https://jitpack.io")
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/CivMC/CombatTagPlus")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }
}
