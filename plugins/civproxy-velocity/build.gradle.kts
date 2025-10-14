plugins {
    alias(libs.plugins.shadow)
}
version = "1.0.0"

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("us.ajg0702.queue.api:api:2.8.0")
    api(libs.hikaricp)
    api(libs.configurate.yaml)
    api("org.mariadb.jdbc:mariadb-java-client:3.5.6")
    compileOnly(libs.luckperms.api)
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}
