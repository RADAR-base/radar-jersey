rootProject.name = "radar-jersey"

include("radar-jersey")
include("radar-jersey-hibernate")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}
