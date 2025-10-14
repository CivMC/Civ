import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension

plugins {
    alias(libs.plugins.paper.userdev) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.runpaper) apply false
}

project.extensions.configure<GradleEnterpriseExtension> {
    buildScan {
        if (System.getenv("CI") != null) {
            tag("CI")
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

allprojects {
    group = "net.civmc"

    repositories {
        mavenCentral()
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public")
        maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
        maven("https://repo.rapture.pw/repository/maven-releases/")
        maven("https://jitpack.io")
        maven("https://repo.ajg0702.us/releases")
    }
}
