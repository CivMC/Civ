plugins {
	id("io.papermc.paperweight.userdev")
	id("com.github.johnrengelman.shadow")
	id("xyz.jpenilla.run-paper")
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

	implementation("co.aikar:acf-bukkit:0.5.0-SNAPSHOT")
	implementation("com.mojang:datafixerupper:1.0.20")
	implementation("com.zaxxer:HikariCP:5.0.1")
	implementation("co.aikar:taskchain-bukkit:3.7.2")
	implementation("com.github.IPVP-MC:canvas:91ec97f076")
	implementation("org.apache.commons:commons-lang3:3.12.0")
	implementation("org.apache.commons:commons-collections4:4.4")
	implementation("com.google.code.findbugs:jsr305:3.0.2")

	compileOnly("it.unimi.dsi:fastutil:8.5.8")

	compileOnly("org.projectlombok:lombok:1.18.24")
	annotationProcessor ("org.projectlombok:lombok:1.18.24")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}
