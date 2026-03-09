import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing
import org.radarbase.gradle.plugin.radarRootProject

plugins {
    alias(libs.plugins.radar.root.project)
    alias(libs.plugins.radar.dependency.management)
}

repositories {
    mavenCentral()
}

radarRootProject {
    projectVersion.set(libs.versions.project)
    gradleVersion.set(libs.versions.gradle)
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")
    apply(plugin = "org.radarbase.radar-publishing")

    dependencies {
        plugins.withType<JavaPlugin> {
            constraints {
                add("implementation", rootProject.libs.jackson.bom) {
                    because("Force safe version of Jackson across all modules")
                }
            }
        }
    }

    radarKotlin {
        javaVersion.set(libs.versions.java.get().toInt())
        kotlinVersion.set(rootProject.libs.versions.kotlin)
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
