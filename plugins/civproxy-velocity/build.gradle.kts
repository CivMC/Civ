version = "1.0.0"

repositories {
    maven {
        url = uri("https://repo.ajg0702.us/releases")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("us.ajg0702.queue.api:api:2.8.0")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}
