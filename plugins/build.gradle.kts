import io.papermc.paperweight.userdev.PaperweightUserExtension
import xyz.jpenilla.runpaper.task.RunServer

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    var javaVersion = 21
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<Javadoc> {
        options {
            (this as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = javaVersion
    }

    tasks.withType<ProcessResources> {
        filteringCharset = "UTF-8"
    }

    tasks.withType<Javadoc> {
        enabled = false
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
            pluginManager.withPlugin("com.gradleup.shadow") {
                create<MavenPublication>("shadow") {
                    from(components["java"])
                }
            }
        }
    }

    tasks.withType<RunServer> {
        minecraftVersion("1.18")
    }

    pluginManager.withPlugin("io.papermc.paperweight.userdev") {
        project.extensions.getByName<PaperweightUserExtension>("paperweight")
            .reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

        tasks.withType<ProcessResources> {
            filesMatching("plugin.yml") {
                expand(project.properties)
            }
        }
    }

    pluginManager.withPlugin("com.gradleup.shadow") {
        tasks {
            named("build") {
                dependsOn("shadowJar")
            }
        }
    }
}
