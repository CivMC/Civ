plugins {
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

repositories {
	fun civRepo(name: String) {
		maven {
			url = uri("https://maven.pkg.github.com/CivMC/${name}")
			credentials {
				username = System.getenv("GITHUB_ACTOR")
				password = System.getenv("GITHUB_TOKEN")
			}
		}
	}

	mavenCentral()
	civRepo("CivModCore")

	maven("https://hub.spigotmc.org/nexus/content/groups/public/")
	maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
	maven("https://repo.md-5.net/content/repositories/public/")
	maven("https://oss.sonatype.org/content/repositories/snapshots")
	maven("https://repo.aikar.co/content/groups/aikar/")
	maven("https://jitpack.io")
}

dependencies {
	paperDevBundle("1.18-R0.1-SNAPSHOT")

	implementation("net.civmc:civmodcore:2.0.0-SNAPSHOT:dev-all")
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
	build {
		dependsOn(reobfJar)
	}

	compileJava {
		options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

		// Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
		// See https://openjdk.java.net/jeps/247 for more information.
		options.release.set(17)
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
	}

	test {
		useJUnitPlatform()
	}
}
