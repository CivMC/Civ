package net.civmc.civgradle.paper

import net.civmc.civgradle.CivGradleExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PlatformPaper {

    private val logger: Logger = LoggerFactory.getLogger(PlatformPaper::class.java)

    fun apply(project: Project, extension: CivGradleExtension) {

        project.afterEvaluate {
            configurePaper(project, extension.paper)
        }
    }

    private fun configurePaper(project: Project, extension: PlatformPaperExtension) {
        project.tasks.findByName("build")?.dependsOn("reobfJar")

        project.tasks.withType(ProcessResources::class.java) {
            it.expand(mapOf(
                "name" to extension.pluginName,
                "version" to project.version
            ))
        }
    }
}
