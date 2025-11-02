
subprojects {
    apply(plugin = "java-library")

    var javaVersion = 21
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = javaVersion
    }

    tasks.withType<ProcessResources> {
        filteringCharset = "UTF-8"
    }

    pluginManager.withPlugin("com.gradleup.shadow") {
        tasks {
            named("build") {
                dependsOn("shadowJar")
            }
        }
    }
}
