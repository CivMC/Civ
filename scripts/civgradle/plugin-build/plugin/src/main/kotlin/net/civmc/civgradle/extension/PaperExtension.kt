
package net.civmc.civgradle.extension

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

@Suppress("unused_parameter")
open class PaperExtension @Inject constructor(objects: ObjectFactory) {

    var paperVersion: String? = null
}