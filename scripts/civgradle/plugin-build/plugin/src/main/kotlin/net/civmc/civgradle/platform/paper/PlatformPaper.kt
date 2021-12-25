package net.civmc.civgradle.platform.paper

import net.civmc.civgradle.extension.CivGradleExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PlatformPaper {

    private val logger: Logger = LoggerFactory.getLogger(PlatformPaper::class.java)

    fun apply(project: Project, extension: CivGradleExtension) {
        val spigotExtension = extension.paper

        logger.info("Using Paper version: ${spigotExtension.paperVersion}")

        configureBuild(project, extension)
    }

    private fun configureBuild(project: Project, extension: CivGradleExtension) {
        // TODO: apply paper userdev
        // project.tasks
        //     .findByName("build")
        //     ?.dependsOn("reobfJar")
    }
}
