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
}
