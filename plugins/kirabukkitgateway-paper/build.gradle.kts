plugins {
    alias(libs.plugins.paper.userdev)
    alias(libs.plugins.shadow)
}

version = "2.0.3"

dependencies {
    paperweight {
        paperDevBundle(libs.versions.paper)
    }

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
    compileOnly(project(":plugins:civchat2-paper"))
    compileOnly(project(":plugins:jukealert-paper"))

    api(libs.rabbitmq.client)
    compileOnly(libs.luckperms.api)
}
