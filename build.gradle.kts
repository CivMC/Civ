import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

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
        maven(url = "https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://libraries.minecraft.net")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public")
        maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
        maven("https://jitpack.io")
        maven("https://repo.ajg0702.us/releases")
    }
}

subprojects {
    project.pluginManager.withPlugin("java") {
        @Suppress("UnstableApiUsage")
        project.extensions.configure<TestingExtension> {
            suites {
                withType<JvmTestSuite>().configureEach {
                    useJUnitJupiter()
                    targets.configureEach {
                        testTask.configure {
                            testLogging {
                                events(*TestLogEvent.entries.toTypedArray())
                                exceptionFormat = TestExceptionFormat.FULL
                                showCauses = true
                                showExceptions = true
                                showStackTraces = true
                            }
                        }
                    }
                }
            }
        }
    }
}
