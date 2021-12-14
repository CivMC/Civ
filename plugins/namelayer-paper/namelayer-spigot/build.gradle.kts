plugins {
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

repositories {
	mavenCentral()
	maven("https://hub.spigotmc.org/nexus/content/groups/public/")
	maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
	maven("https://repo.md-5.net/content/repositories/public/")
	maven("https://oss.sonatype.org/content/repositories/snapshots")
	maven("https://repo.aikar.co/content/groups/aikar/")
	maven("https://jitpack.io")
}

dependencies {
	paperDevBundle("1.18-R0.1-SNAPSHOT")

	implementation("com.github.CivMC:CivModCore:v2.0.0-SNAPSHOT-11:dev-all")
}

configure<JavaPluginExtension> {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile> {
	options.encoding = Charsets.UTF_8.name()
	options.release.set(17)
}

tasks.withType<Javadoc> {
	options.encoding = Charsets.UTF_8.name()
}

tasks.withType<ProcessResources> {
	filteringCharset = Charsets.UTF_8.name()
}
