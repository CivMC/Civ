plugins {
    id("io.papermc.paperweight.userdev")
    id("com.github.johnrengelman.shadow")
}

version = "2.0.3"

dependencies {
    paperweight {
        paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    }

    api("com.rabbitmq:amqp-client:5.17.1")
    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:civchat2-paper"))
    compileOnly(project(":plugins:jukealert-paper"))
    compileOnly("net.luckperms:api:5.0")
}
