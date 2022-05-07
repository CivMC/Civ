import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val pluginName: String by project

plugins {
	`java-library`
	id("com.adarshr.test-logger") version "3.2.0" apply false
	id("com.github.johnrengelman.shadow") version "7.1.0" apply false
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")
	apply(plugin = "com.adarshr.test-logger")

	project.setProperty("archivesBaseName", "$pluginName-$name")

	configure<JavaPluginExtension> {
		withJavadocJar()
		withSourcesJar()

		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}

	repositories {
		mavenCentral()
		maven("https://oss.sonatype.org/content/repositories/snapshots")
	}

	dependencies {
		testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
		testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	afterEvaluate {
		tasks.withType<ShadowJar> {
			// Overwrite default jar
			archiveClassifier.set("")
		}

		tasks.findByName("shadowJar")?.also {
			tasks.named("assemble") { dependsOn(it) }
		}
	}

	configure<PublishingExtension> {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://maven.pkg.github.com/CivMC/$pluginName")
				credentials {
					username = System.getenv("GITHUB_ACTOR")
					password = System.getenv("GITHUB_TOKEN")
				}
			}

			val targetRepo = if (version.toString().endsWith("SNAPSHOT")) "maven-snapshots" else "maven-releases"
			maven {
				name = "CivMC"
				url = uri("https://repo.civmc.net/repository/$targetRepo/")
				credentials {
					username = System.getenv("CIVMC_NEXUS_USER")
					password = System.getenv("CIVMC_NEXUS_PASSWORD")
				}
			}
		}
		publications {
			register<MavenPublication>("mavenJava") {
				from(components["java"])
			}
		}
	}
}
