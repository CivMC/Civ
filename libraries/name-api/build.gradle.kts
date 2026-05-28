plugins {
    id("java")
}

group = "net.civmc"
version = "1.0.0"

dependencies {
    api(libs.hikaricp)
    api(libs.configurate.yaml)
    api(libs.mariadb.client)
    implementation(libs.slf4j.api)
    api(project(":libraries:migrations-manager"))
}
