plugins {
    java
    application
    alias(libs.plugins.shadow)
}

group = "xyz.huskydog"
version = "1.0.0"

dependencies {
    implementation(libs.minestom)
    implementation(libs.logback.classic)
}

application {
    mainClass = "xyz.huskydog.queue.Main"
    applicationDefaultJvmArgs = listOf(
        "-Xms1G",
        "-Xmx1G",
        "-XX:+UseG1GC",
        "-XX:G1HeapRegionSize=4M",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+ParallelRefProcEnabled",
        "-XX:+AlwaysPreTouch",
        "-Dlog4j2.formatMsgNoLookups=true",

//        "-Dport=25571",
//        "-Dproxy=VELOCITY",
//        "-DvelocitySecret=1234567890ABCDEF"
    )
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
//        from(file("LICENCE")) {
//            rename { "LICENSE_PistomQueue" } // Use US spelling
//        }
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
    }
}
