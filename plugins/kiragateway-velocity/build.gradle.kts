import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.shadow)
}

group = "xyz.huskydog"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    implementation(libs.configurate.yaml)
    api(libs.rabbitmq.client)
    compileOnly(libs.luckperms.api)
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates by tasks.registering(Copy::class) {
    val props = mapOf(
        "version" to project.version
    )
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.named("main") {
    java.srcDir(generateTemplates.map { it.outputs })
}
