val paperPlugin by configurations.creating
val pvpPlugin by configurations.creating
val proxyPlugin by configurations.creating

dependencies {
    paperPlugin(project(path = ":plugins:banstick-paper", configuration = "shadow"))
    paperPlugin(project(path = ":plugins:bastion-paper"))
    paperPlugin(project(path = ":plugins:castlegates-paper"))
    paperPlugin(project(path = ":plugins:citadel-paper"))
    paperPlugin(project(path = ":plugins:civchat2-paper"))
    paperPlugin(project(path = ":plugins:civduties-paper"))
    paperPlugin(project(path = ":plugins:civmodcore-paper", configuration = "shadow"))
    paperPlugin(project(path = ":plugins:combattagplus-paper"))
    paperPlugin(project(path = ":plugins:donum-paper"))
    paperPlugin(project(path = ":plugins:essenceglue-paper"))
    paperPlugin(project(path = ":plugins:exilepearl-paper"))
    paperPlugin(project(path = ":plugins:factorymod-paper"))
    paperPlugin(project(path = ":plugins:hiddenore-paper"))
    paperPlugin(project(path = ":plugins:itemexchange-paper"))
    paperPlugin(project(path = ":plugins:jukealert-paper"))
    paperPlugin(project(path = ":plugins:kirabukkitgateway-paper", configuration = "shadow"))
    paperPlugin(project(path = ":plugins:namecolors-paper"))
    paperPlugin(project(path = ":plugins:namelayer-paper"))
    paperPlugin(project(path = ":plugins:railswitch-paper"))
    paperPlugin(project(path = ":plugins:randomspawn-paper"))
    paperPlugin(project(path = ":plugins:realisticbiomes-paper"))
    paperPlugin(project(path = ":plugins:simpleadminhacks-paper"))
    paperPlugin(project(path = ":plugins:heliodor-paper"))

    pvpPlugin(project(path = ":plugins:banstick-paper", configuration = "shadow"))
    pvpPlugin(project(path = ":plugins:civduties-paper"))
    pvpPlugin(project(path = ":plugins:civmodcore-paper", configuration = "shadow"))
    pvpPlugin(project(path = ":plugins:combattagplus-paper"))
    pvpPlugin(project(path = ":plugins:finale-paper"))
    pvpPlugin(project(path = ":plugins:simpleadminhacks-paper"))
    pvpPlugin(project(path = ":plugins:kitpvp-paper"))
    pvpPlugin(project(path = ":plugins:voidworld-paper"))
    pvpPlugin(project(path = ":plugins:heliodor-paper"))
}

val copyPaperPlugins = tasks.register<Copy>("copyPaperPlugins") {
    dependsOn(paperPlugin)

    doFirst {
        project.delete(files("$buildDir/paper-plugins"))
    }

    from("$projectDir/src/paper-plugins")
    from(paperPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$buildDir/paper-plugins")
}

val copyPvpPlugins = tasks.register<Copy>("copyPvpPlugins") {
    dependsOn(pvpPlugin)

    doFirst {
        project.delete(files("$buildDir/pvp-plugins"))
    }

    from("$projectDir/src/pvp-plugins")
    from(pvpPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$buildDir/pvp-plugins")
}

val copyProxyPlugins = tasks.register<Copy>("copyProxyPlugins") {
    dependsOn(proxyPlugin)

    doFirst {
        project.delete(files("$buildDir/proxy-plugins"))
    }

    from("$projectDir/src/proxy-plugins")
    from(proxyPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$buildDir/proxy-plugins")
}

// TODO: Is build the right name?
tasks.register("build") {
    dependsOn(copyPaperPlugins)
    dependsOn(copyPvpPlugins)
    dependsOn(copyProxyPlugins)
}
