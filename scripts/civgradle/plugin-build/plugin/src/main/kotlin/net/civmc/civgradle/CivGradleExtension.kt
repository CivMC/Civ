package net.civmc.civgradle

import org.gradle.api.Project
import java.net.URI
import javax.inject.Inject

abstract class CivGradleExtension @Inject constructor(val project: Project) {

    var githubOrganization = "CivMC"

    var civRepositories = listOf<String>()

    /**
     * Add a dependency from the CivMC Github maven repositories.
     * Note that with this, GITHUB_ACTOR and GITHUB_TOKEN _must_ be set on the host system.
     */
    fun civRepository(name: String) {

    }
}