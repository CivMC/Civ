package net.civmc.civgradle

import org.gradle.api.GradleException
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import java.net.URI

abstract class CivGradlePlugin : Plugin<Project> {

    private val logger: Logger = LoggerFactory.getLogger(CivGradlePlugin::class.java)

    override fun apply(project: Project) {
        val extension = project.extensions.create("civGradle", CivGradleExtension::class.java, project)

        configureJava(project)
        configureCivRepositories(project, extension)
    }

    fun configureCivRepositories(project: Project, extension: CivGradleExtension) {
        if (extension.civRepositories.isEmpty()) {
            logger.debug("No civ repositories to configure")
        }

        val githubActor: String? = System.getenv("GITHUB_ACTOR")
        val githubToken: String? = System.getenv("GITHUB_TOKEN")

        if (githubActor.isNullOrEmpty() || githubToken.isNullOrEmpty()) {
            throw GradleException("GITHUB_ACTOR or GITHUB_TOKEN are not configured. Please set them in environment variables.")
        }

        extension.civRepositories.forEach { name ->
            project.repositories.maven {
                it.url = URI("https://maven.pkg.github.com/${extension.githubOrganization}/${name}")
                it.credentials {
                    // These need to be set in the user environment variables
                    it.username = githubActor
                    it.password = githubToken
                }
            }
        }

        logger.debug("Civ Repositories Configured")
    }

    /**
     * Configure our project to use java 17 UTF_8 for everything
     */
    fun configureJava(project: Project) {
        project.pluginManager.apply(JavaLibraryPlugin::class.java)

        val javaExtension = project.extensions.findByType(JavaPluginExtension::class.java)

        javaExtension?.toolchain?.languageVersion?.set(JavaLanguageVersion.of(17))

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
