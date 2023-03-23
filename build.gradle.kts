import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.radarbase.gradle.plugin.radarPublishing
import org.radarbase.gradle.plugin.radarRootProject

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

    dependencies {
        val log4j2Version = Versions.log4j2
        val testRuntimeOnly by configurations
        testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-core:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")
    }

    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
            exceptionFormat = FULL
        }
        useJUnitPlatform()
        systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
    }
}
