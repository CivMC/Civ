
package net.civmc.civgradle.paper

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

@Suppress("unused_parameter")
open class PlatformPaperExtension @Inject constructor(objects: ObjectFactory) {

    var pluginName = ""
}
