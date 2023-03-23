import java.util.*

rootProject.name = "radar-jersey"

include("radar-jersey")
include("radar-jersey-hibernate")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://maven.pkg.github.com/radar-base/radar-commons") {
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: extra.properties["gpr.user"] as? String
                    ?: extra.properties["public.gpr.user"] as? String
                password = System.getenv("GITHUB_TOKEN")
                    ?: extra.properties["gpr.token"] as? String
                    ?: (extra.properties["public.gpr.token"] as? String)?.let {
                        Base64.getDecoder().decode(it).decodeToString()
                    }
            }
        }
    }

    plugins {
        val radarCommonsVersion = "0.16.0-SNAPSHOT"
        id("org.radarbase.radar-root-project") version radarCommonsVersion
        id("org.radarbase.radar-dependency-management") version radarCommonsVersion
        id("org.radarbase.radar-kotlin") version radarCommonsVersion
        id("org.radarbase.radar-publishing") version radarCommonsVersion

    }
}
