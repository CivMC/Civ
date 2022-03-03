repositories {
	maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
	implementation(project(":api"))

	compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
}
