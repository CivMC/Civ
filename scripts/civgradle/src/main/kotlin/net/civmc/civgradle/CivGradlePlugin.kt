package net.civmc.civgradle

import net.civmc.civgradle.common.PlatformCommon
import net.civmc.civgradle.paper.PlatformPaper
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
abstract class CivGradlePlugin : Plugin<Project> {

    private val logger: Logger = LoggerFactory.getLogger(CivGradlePlugin::class.java)

    override fun apply(project: Project) {
        val extension = project.extensions.create("civGradle", CivGradleExtension::class.java)

        project.beforeEvaluate {
            PlatformCommon.beforeEvaluate(project, extension)
            PlatformPaper.beforeEvaluate(project, extension)
        }

        project.afterEvaluate {
            PlatformCommon.afterEvaluate(project, extension)
            PlatformPaper.afterEvaluate(project, extension)
        }
    }
}
