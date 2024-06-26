import io.papermc.paperweight.tasks.RemapJar
import xyz.jpenilla.runpaper.task.RunServer

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    tasks.withType<ProcessResources> {
        filteringCharset = "UTF-8"
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
                pluginManager.withPlugin("io.papermc.paperweight.userdev") {
                    artifact(project.tasks.withType<RemapJar>().getByName("reobfJar").outputJar)
                }
            }
            pluginManager.withPlugin("com.github.johnrengelman.shadow") {
                create<MavenPublication>("shadow") {
                    from(components["java"])
                    pluginManager.withPlugin("io.papermc.paperweight.userdev") {
                        artifact(project.tasks.withType<RemapJar>().getByName("reobfJar").outputJar)
                    }
                }
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
