import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension

plugins {
    id("io.papermc.paperweight.userdev") version "1.7.2" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("xyz.jpenilla.run-paper") version "2.2.2" apply false
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
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public")

        maven("https://jitpack.io")
    }
}
