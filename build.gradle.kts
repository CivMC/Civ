import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("io.papermc.paperweight.userdev") version "1.5.10" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("xyz.jpenilla.run-paper") version "2.2.2" apply false
}

project.extensions.configure<GradleEnterpriseExtension> {
    buildScan {
        if (System.getenv("CI") != null) {
            tag("CI")
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

allprojects {

    group = "net.civmc"

    // TODO: We probably don't want to apply these to every project
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.io/repository/maven-public/")

        maven("https://jitpack.io")
    }

    pluginManager.withPlugin("java-library") {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
            withSourcesJar()
            withJavadocJar()
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 17
        }

        tasks.withType<ProcessResources> {
            filteringCharset = "UTF-8"
        }
    }


    configure<PublishingExtension> {
        val githubActor = System.getenv("GITHUB_ACTOR")
        val githubToken = System.getenv("GITHUB_TOKEN")

        repositories {
            if (githubActor != null && githubToken != null) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/CivMC/Civ")
                    credentials {
                        username = githubActor
                        password = githubToken
                    }
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    tasks.withType<RunServer> {
        minecraftVersion("1.18")
    }

    pluginManager.withPlugin("io.papermc.paperweight.userdev") {
        tasks.withType<ProcessResources> {
            filesMatching("plugin.yml") {
                expand(project.properties)
            }
        }

        tasks {
            named("build") {
                dependsOn("reobfJar")
            }
        }
    }

    pluginManager.withPlugin("com.github.johnrengelman.shadow") {
        tasks {
            named("build") {
                dependsOn("shadowJar")
            }
        }
    }
}