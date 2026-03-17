import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing
import org.radarbase.gradle.plugin.radarRootProject

plugins {
    alias(libs.plugins.radar.root.project)
    alias(libs.plugins.radar.dependency.management)
    alias(libs.plugins.radar.kotlin) apply false
    alias(libs.plugins.radar.publishing) apply false
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

    // --- Vulnerability fixes start ---
    dependencies {
        plugins.withType<JavaPlugin> {
            constraints {
                add("implementation", rootProject.libs.jackson.bom) {
                    because("Force safe version of Jackson across all modules")
                }
                add("implementation", rootProject.libs.jackson.core) {
                    because("Force safe version of Jackson across all modules")
                }
            }
        }
    }
    // --- Vulnerability fixes end ---

    radarKotlin {
        log4j2Version.set(rootProject.libs.versions.log4j2)
    }

    radarPublishing {
        val githubRepoName = "RADAR-base/radar-jersey"
        githubUrl.set("https://github.com/$githubRepoName.git")
        developers {
            developer {
                id.set("pvannierop")
                name.set("Pim van Nierop")
                email.set("pim@thehyve.nl")
                organization.set("The Hyve")
            }
            developer {
                id.set("mpgxvii")
                name.set("Pauline Conde")
                email.set("mpgxvii@gmail.com")
                organization.set("King's College London")
            }
            developer {
                id.set("this-Aditya")
                name.set("Aditya Mishra")
                email.set("aditya.mishra@kcl.ac.uk")
                organization.set("King's College London")
            }
        }
    }
}
