rootProject.name = "radar-jersey"

include("radar-jersey")
include("radar-jersey-hibernate")

pluginManagement {
    val kotlinVersion: String by settings
    val dokkaVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.jfrog.bintray") version "1.8.5"
        id("org.jetbrains.dokka") version dokkaVersion
    }
}
