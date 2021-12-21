subprojects {
    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()

    group = "net.civmc"
    version = "2.0.0-SNAPSHOT"
    description = "CombatTagPlus"

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
        configure<PublishingExtension> {
            publications {
                register<MavenPublication>("gpr") {
                    from(components["java"])
                }
            }
        }
    }
}
