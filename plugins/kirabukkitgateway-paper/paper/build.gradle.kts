plugins {
	id("net.civmc.civgradle")
	id("io.papermc.paperweight.userdev")
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    implementation("com.rabbitmq:amqp-client:5.6.0")
    compileOnly("net.civmc.civmodcore:civmodcore-paper:2.3.5:dev-all")
	compileOnly("net.civmc.namelayer:namelayer-paper:3.0.4:dev")
	compileOnly("net.civmc.civchat2:civchat2-paper:2.0.3:dev")
	compileOnly("net.civmc.jukealert:jukealert-paper:3.0.5:dev")
	compileOnly("net.luckperms:api:5.0")
}
