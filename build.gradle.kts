import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.radarbase.gradle.plugin.radarPublishing
import org.radarbase.gradle.plugin.radarRootProject
import org.radarbase.gradle.plugin.radarKotlin

plugins {
    id("org.radarbase.radar-root-project") version Versions.radarCommons
    id("org.radarbase.radar-dependency-management") version Versions.radarCommons
    id("org.radarbase.radar-kotlin") version Versions.radarCommons apply false
    id("org.radarbase.radar-publishing") version Versions.radarCommons apply false
}

radarRootProject {
    projectVersion.set("0.11.0-SNAPSHOT")
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")
    apply(plugin = "org.radarbase.radar-publishing")

    radarKotlin {
        log4j2Version.set(Versions.log4j2)
        slf4jVersion.set(Versions.slf4j)
    }

    radarPublishing {
        val githubRepoName = "RADAR-base/radar-jersey"
        githubUrl.set("https://github.com/$githubRepoName.git")
        developers {
            developer {
                id.set("blootsvoets")
                name.set("Joris Borgdorff")
                email.set("joris@thehyve.nl")
                organization.set("The Hyve")
            }
            developer {
                id.set("nivemaham")
                name.set("Nivethika Mahasivam")
                email.set("nivethika@thehyve.nl")
                organization.set("The Hyve")
            }
        }
    }
}
