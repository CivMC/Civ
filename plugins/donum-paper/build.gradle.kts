version = "2.0.0"

dependencies {
    compileOnly(libs.paper.api)

    compileOnly(project(":plugins:civmodcore-paper"))
    compileOnly(project(":plugins:namelayer-paper"))
}
