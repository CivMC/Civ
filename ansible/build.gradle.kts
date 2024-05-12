val paperPlugin by configurations.creating
val proxyPlugin by configurations.creating

dependencies {
    paperPlugin(project(path = ":plugins:banstick-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:bastion-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:castlegates-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:citadel-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:civchat2-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:civduties-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:civmodcore-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:combattagplus-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:donum-paper"))
    paperPlugin(project(path = ":plugins:essenceglue-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:exilepearl-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:factorymod-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:finale-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:hiddenore-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:itemexchange-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:jukealert-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:kirabukkitgateway-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:namecolors-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:namelayer-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:railswitch-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:randomspawn-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:realisticbiomes-paper", configuration = "reobf"))
    paperPlugin(project(path = ":plugins:simpleadminhacks-paper", configuration = "reobf"))
}

val copyPaperPlugins = tasks.register<Copy>("copyPaperPlugins") {
    dependsOn(paperPlugin)

    from("$projectDir/src/paper-plugins")
    from(paperPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$buildDir/paper-plugins")
}

val copyProxyPlugins = tasks.register<Copy>("copyProxyPlugins") {
    dependsOn(proxyPlugin)

    from("$projectDir/src/proxy-plugins")
    from(proxyPlugin.resolvedConfiguration.resolvedArtifacts.map { it.file })
    into("$buildDir/proxy-plugins")
}

// TODO: Is build the right name?
tasks.register("build") {
    dependsOn(copyPaperPlugins)
    dependsOn(copyProxyPlugins)
}
