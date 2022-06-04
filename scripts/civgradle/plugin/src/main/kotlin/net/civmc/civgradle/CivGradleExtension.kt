package net.civmc.civgradle

import net.civmc.civgradle.paper.PlatformPaperExtension
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class CivGradleExtension @Inject constructor(objects: ObjectFactory) {

    var pluginName = ""

    val paper: PlatformPaperExtension = objects.newInstance(PlatformPaperExtension::class.java)

    @Suppress("unused")
    fun paper(action: Action<PlatformPaperExtension>) {
        action.execute(paper)
    }
}

