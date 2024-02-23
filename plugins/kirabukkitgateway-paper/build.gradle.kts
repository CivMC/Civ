plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.0.3"

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    implementation("com.rabbitmq:amqp-client:5.6.0")
	compileOnly(project(":plugins:civmodcore-paper"))
	compileOnly(project(":plugins:namelayer-paper"))
	compileOnly(project(":plugins:civchat2-paper"))
	compileOnly(project(":plugins:jukealert-paper"))
	compileOnly("net.luckperms:api:5.0")
}
