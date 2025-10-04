val paperPlugin by configurations.creating
val gammaPlugin by configurations.creating
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
    paperPlugin(project(path = ":plugins:finale-paper"))
    paperPlugin(project(path = ":plugins:combattagplus-paper"))
    paperPlugin(project(path = ":plugins:donum-paper"))
    paperPlugin(project(path = ":plugins:essenceglue-paper"))
    paperPlugin(project(path = ":plugins:exilepearl-paper"))
    paperPlugin(project(path = ":plugins:factorymod-paper"))
    paperPlugin(project(path = ":plugins:hiddenore-paper"))
    paperPlugin(project(path = ":plugins:itemexchange-paper"))
    paperPlugin(project(path = ":plugins:jukealert-paper", configuration = "shadow"))
    paperPlugin(project(path = ":plugins:kirabukkitgateway-paper", configuration = "shadow"))
    paperPlugin(project(path = ":plugins:namecolors-paper"))
    paperPlugin(project(path = ":plugins:namelayer-paper"))
    paperPlugin(project(path = ":plugins:railswitch-paper"))
    paperPlugin(project(path = ":plugins:randomspawn-paper"))
    paperPlugin(project(path = ":plugins:realisticbiomes-paper"))
    paperPlugin(project(path = ":plugins:simpleadminhacks-paper"))
    paperPlugin(project(path = ":plugins:heliodor-paper"))

    gammaPlugin(project(path = ":plugins:banstick-paper", configuration = "shadow"))
    gammaPlugin(project(path = ":plugins:bastion-paper"))
    gammaPlugin(project(path = ":plugins:castlegates-paper"))
    gammaPlugin(project(path = ":plugins:citadel-paper"))
    gammaPlugin(project(path = ":plugins:civchat2-paper"))
    gammaPlugin(project(path = ":plugins:civduties-paper"))
    gammaPlugin(project(path = ":plugins:civmodcore-paper", configuration = "shadow"))
    gammaPlugin(project(path = ":plugins:finale-paper"))
    gammaPlugin(project(path = ":plugins:combattagplus-paper"))
    gammaPlugin(project(path = ":plugins:donum-paper"))
    gammaPlugin(project(path = ":plugins:essenceglue-paper"))
    gammaPlugin(project(path = ":plugins:exilepearl-paper"))
    gammaPlugin(project(path = ":plugins:factorymod-paper"))
    gammaPlugin(project(path = ":plugins:hiddenore-paper"))
    gammaPlugin(project(path = ":plugins:itemexchange-paper"))
    gammaPlugin(project(path = ":plugins:jukealert-paper", configuration = "shadow"))
    gammaPlugin(project(path = ":plugins:namecolors-paper"))
    gammaPlugin(project(path = ":plugins:namelayer-paper"))
    gammaPlugin(project(path = ":plugins:railswitch-paper"))
    gammaPlugin(project(path = ":plugins:randomspawn-paper"))
    gammaPlugin(project(path = ":plugins:realisticbiomes2-paper"))
    gammaPlugin(project(path = ":plugins:simpleadminhacks-paper"))
    gammaPlugin(project(path = ":plugins:heliodor-paper"))

    pvpPlugin(project(path = ":plugins:banstick-paper", configuration = "shadow"))
    pvpPlugin(project(path = ":plugins:civduties-paper"))
    pvpPlugin(project(path = ":plugins:civmodcore-paper", configuration = "shadow"))
    pvpPlugin(project(path = ":plugins:finale-paper"))
    pvpPlugin(project(path = ":plugins:simpleadminhacks-paper"))
    pvpPlugin(project(path = ":plugins:kitpvp-paper"))
    pvpPlugin(project(path = ":plugins:voidworld-paper"))
    pvpPlugin(project(path = ":plugins:heliodor-paper"))
    pvpPlugin(project(path = ":plugins:namelayer-paper"))
    pvpPlugin(project(path = ":plugins:civchat2-paper"))
    pvpPlugin(project(path = ":plugins:namecolors-paper"))

    proxyPlugin(project(path = ":plugins:civproxy-velocity"))
    proxyPlugin(project(path = ":plugins:announcements-velocity", configuration = "shadow"))
    proxyPlugin(project(path = ":plugins:kiragateway-velocity", configuration = "shadow"))
}

val copyPaperPlugins = tasks.register<Copy>("copyPaperPlugins") {
    dependsOn(paperPlugin)

    doFirst {
        project.delete(files("$projectDir/build/paper-plugins"))
    }

    from("$projectDir/src/paper-plugins")
    from(paperPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$buildDir/paper-plugins")
}

val copyPvpPlugins = tasks.register<Copy>("copyPvpPlugins") {
    dependsOn(pvpPlugin)

    doFirst {
        project.delete(files("$projectDir/build/pvp-plugins"))
    }

    from("$projectDir/src/pvp-plugins")
    from(pvpPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$projectDir/build/pvp-plugins")
}

val copyGammaPlugins = tasks.register<Copy>("copyGammaPlugins") {
    dependsOn(gammaPlugin)

    doFirst {
        project.delete(files("$projectDir/build/gamma-plugins"))
    }

    from("$projectDir/src/gamma-plugins")
    from(gammaPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$projectDir/build/gamma-plugins")
}

val copyProxyPlugins = tasks.register<Copy>("copyProxyPlugins") {
    dependsOn(proxyPlugin)

    doFirst {
        project.delete(files("$projectDir/build/proxy-plugins"))
    }

    from("$projectDir/src/proxy-plugins")
    from(proxyPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$projectDir/build/proxy-plugins")
}

// TODO: Is build the right name?
tasks.register("build") {
    dependsOn(copyPaperPlugins)
    dependsOn(copyGammaPlugins)
    dependsOn(copyPvpPlugins)
    dependsOn(copyProxyPlugins)
}
