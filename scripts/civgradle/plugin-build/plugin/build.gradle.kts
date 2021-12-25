plugins {
    kotlin("jvm") version "1.6.0"
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.6.0"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

gradlePlugin {
    plugins {
        create("net.civmc.civgradle") {
            id = "net.civmc.civgradle.plugin"
            implementationClass = "net.civmc.civgradle.CivGradlePlugin"
            version = version
        }
    }
}