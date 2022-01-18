plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
	id("com.github.johnrengelman.shadow") version "7.1.0"
}

civGradle {
	paper {
		pluginName = "CivModCore"
	}
}

dependencies {
	paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    	implementation("co.aikar:acf-bukkit:0.5.0-SNAPSHOT")
    	implementation("com.mojang:datafixerupper:1.0.20")
        implementation("com.zaxxer:HikariCP:3.4.2")
        implementation("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT")
        implementation("co.aikar:taskchain-bukkit:3.7.2")
        implementation("com.github.IPVP-MC:canvas:91ec97f076")
        implementation("org.apache.commons:commons-lang3:3.12.0")
        implementation("org.apache.commons:commons-collections4:4.4")
        implementation("com.google.code.findbugs:jsr305:3.0.2")

        compileOnly("it.unimi.dsi:fastutil:8.2.2")

    	compileOnly("org.projectlombok:lombok:1.18.20")
    	annotationProcessor ("org.projectlombok:lombok:1.18.20")

    	testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}
