plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.civmc.civmodcore:civmodcore-paper:2.3.5:dev-all")
    compileOnly("net.civmc.bastion:bastion-paper:3.0.1:dev")
    compileOnly("net.civmc.banstick:banstick-paper:2.0.1:dev")
}
