plugins {
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
	id("xyz.jpenilla.run-paper")
}

apply(from = "${rootProject.projectDir}/../../scripts/gradle/paper.gradle")

repositories {
	maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
	implementation(project(":api"))

	paperweight {
		paperDevBundle("1.18.2-R0.1-SNAPSHOT")
	}
}

tasks {
	shadowJar {
		exclude("org.slf4j")

		relocate("org.postgresql", "com.programmerdan.minecraft.civspy.repack.postgresql")
		relocate("org.checkerframework", "com.programmerdan.minecraft.civspy.repack.checkerframework")
		relocate("com.zaxxer.hikari", "com.programmerdan.minecraft.civspy.repack.hikari")
	}
}
