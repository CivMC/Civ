plugins {
	id("io.papermc.paperweight.userdev")
}

version = "2.0.0-SNAPSHOT"

dependencies {
	paperweight {
		paperDevBundle(libs.versions.paper)
	}
}
