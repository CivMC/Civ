package net.civmc.civgradle.common

import net.civmc.civgradle.CivGradleExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PlatformCommon {

    private val logger: Logger = LoggerFactory.getLogger(PlatformCommon::class.java)

    fun apply(project: Project, extension: CivGradleExtension) {
            if (project.pluginManager.hasPlugin("java-library")) {
                configureJava(project)
            }
    }

    /**
     * Configure our project to use java 17 UTF_8 for everything
     */
    private fun configureJava(project: Project) {
        val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java)

        javaExtension?.toolchain?.languageVersion?.set(JavaLanguageVersion.of(17))
        javaExtension?.withJavadocJar()
        javaExtension?.withSourcesJar()

        project.tasks.withType(JavaCompile::class.java) {
            it.options.encoding = Charsets.UTF_8.name()
            it.options.release.set(17)
        }

        project.tasks.withType(Javadoc::class.java) {
            it.options.encoding = Charsets.UTF_8.name()
        }

        project.tasks.withType(ProcessResources::class.java) {
            it.filteringCharset = Charsets.UTF_8.name()
        }

        logger.debug("Java tasks configured")
    }
}
