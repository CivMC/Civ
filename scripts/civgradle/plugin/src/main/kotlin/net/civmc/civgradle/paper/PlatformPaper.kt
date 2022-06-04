package net.civmc.civgradle.paper

import net.civmc.civgradle.CivGradleExtension
import org.gradle.api.Project
import org.gradle.api.internal.file.DefaultFilePropertyFactory
import org.gradle.language.jvm.tasks.ProcessResources
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object PlatformPaper {

    private val logger: Logger = LoggerFactory.getLogger(PlatformPaper::class.java)

    fun apply(project: Project, extension: CivGradleExtension) {
        if (project.pluginManager.hasPlugin("io.paperweight.userdev")) {
            logger.debug("Configuring paperweight")
            configurePaperWeight(project, extension)
        }
    }

    private fun configurePaperWeight(project: Project, extension: CivGradleExtension) {
        project.tasks.getByName("build").dependsOn("reobfJar")

        project.setProperty("archivesBaseName", extension.pluginName + "-paper")

        // Reobf jar doesn't use archivesBaseName for some godforsaken reason
        project.tasks.getByName("reobfJar").run {
            // Yeah uh...
            val oldName = (this.property("outputJar") as DefaultFilePropertyFactory.DefaultRegularFileVar)
                .asFile.get().absoluteFile.path

            // Hacky.
            setProperty("outputJar",
                File(oldName.replace(
                    "${project.name}-${project.version}",
                    "${extension.pluginName}-paper-${project.version}"
                ))
            )
        }

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
