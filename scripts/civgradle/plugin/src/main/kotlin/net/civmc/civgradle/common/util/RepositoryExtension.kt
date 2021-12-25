package net.civmc.civgradle.common.util

import org.gradle.api.GradleException
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.civRepo(location: String) {
    val githubActor: String? = System.getenv("GITHUB_ACTOR")
    val githubToken: String? = System.getenv("GITHUB_TOKEN")

    if (githubActor.isNullOrEmpty() || githubToken.isNullOrEmpty()) {
        throw GradleException("GITHUB_ACTOR or GITHUB_TOKEN are not configured. Please set them in environment variables.")
    }

    maven {
        it.url = URI("https://maven.pkg.github.com/${location}")
        it.credentials {
            // These need to be set in the user environment variables
            it.username = githubActor
            it.password = githubToken
        }
    }
}