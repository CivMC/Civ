package net.civmc.civgradle.paper

import net.civmc.civgradle.CivGradleExtension
import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.jpenilla.runpaper.task.RunServerTask

object PlatformPaper {

    private val logger: Logger = LoggerFactory.getLogger(PlatformPaper::class.java)

    fun beforeEvaluate(project: Project, extension: CivGradleExtension) {
        if (project.pluginManager.hasPlugin("xyz.jpenilla.run-paper")) {
            logger.debug("Configuring run-paper")

            project.tasks.withType(RunServerTask::class.java) {
                it.minecraftVersion.set("1.18")
            }
        }
    }

    fun afterEvaluate(project: Project, extension: CivGradleExtension) {
        if (project.pluginManager.hasPlugin("io.papermc.paperweight.userdev")) {
            logger.debug("Configuring paperweight")

            project.tasks.withType(ProcessResources::class.java) {
                it.filesMatching("plugin.yml") {
                    it.expand(mapOf(
                        "name" to extension.pluginName,
                        "version" to project.version
                    ))
                }
            }
        }
    }
}
