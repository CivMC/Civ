plugins {
    id("java")
}

group = "xyz.huskydog"
version = "1.0.0"

dependencies {
    implementation(libs.hikaricp)
    implementation(libs.ipaddress)
    implementation(libs.jsoup)
    implementation(libs.mariadb.java.client)
    implementation(libs.configurate.yaml)
    implementation(libs.slf4j.api)
    implementation(libs.commons.lang3)
    implementation(libs.commons.collections4)
    implementation(libs.google.guava)
    implementation(libs.adventure.api)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.text.serializer.plain)
}
