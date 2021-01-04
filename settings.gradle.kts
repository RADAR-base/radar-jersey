rootProject.name = "radar-jersey"

include("radar-jersey")
include("radar-jersey-hibernate")

pluginManagement {
    val kotlinVersion: String by settings
    val dokkaVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.dokka") version dokkaVersion
    }
}
