package net.civmc.civgradle.extension

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class CivGradleExtension @Inject constructor(objects: ObjectFactory) {

    var githubOrganization = "CivMC"

    var civRepositories = listOf<String>()

    val paper: PaperExtension = objects.newInstance(PaperExtension::class.java)

    @Suppress("unused")
    fun paper(action: Action<PaperExtension>) {
        action.execute(paper)
    }
}

