plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.civmc.civmodcore:paper:2.0.0-SNAPSHOT:dev-all")
    compileOnly("net.civmc:worldborder:2.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:bastion:3.0.0-SNAPSHOT:dev")
    compileOnly("net.civmc:banstick:2.0.0-SNAPSHOT:dev")
}
