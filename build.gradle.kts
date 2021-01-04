import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    id("maven-publish")
    id("org.jetbrains.dokka") apply false
}

subprojects {
    val myproject = this
    group = "org.radarbase"
    version = "0.4.3"

    val githubRepoName = "RADAR-base/radar-jersey"
    val githubUrl = "https://github.com/$githubRepoName.git"
    val githubIssueUrl = "https://github.com/$githubRepoName/issues"

    extra.apply {
        set("githubRepoName", githubRepoName)
        set("githubUrl", githubUrl)
        set("githubIssueUrl", githubIssueUrl)
    }

    repositories {
        jcenter()
        maven(url = "https://dl.bintray.com/radar-cns/org.radarcns")
        maven(url = "https://repo.thehyve.nl/content/repositories/snapshots")
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    configurations {
        // Avoid non-release versions from wildcards
        all {
            val versionSelectorScheme = serviceOf<VersionSelectorScheme>()
            resolutionStrategy.componentSelection.all {
                if (candidate.version.contains("-SNAPSHOT")
                        || candidate.version.contains("-rc", ignoreCase = true)
                        || candidate.version.contains(".Draft", ignoreCase = true)
                        || candidate.version.contains("-alpha", ignoreCase = true)
                        || candidate.version.contains("-beta", ignoreCase = true)) {
                    val dependency = allDependencies.find { it.group == candidate.group && it.name == candidate.module }
                    if (dependency != null && !versionSelectorScheme.parseSelector(dependency.version).matchesUniqueVersion()) {
                        reject("only releases are allowed for $dependency")
                    }
                }
            }
        }
    }

    val sourcesJar by tasks.registering(Jar::class) {
        from(myproject.the<SourceSetContainer>()["main"].allSource)
        archiveClassifier.set("sources")
        val classes by tasks
        dependsOn(classes)
    }

    apply(plugin = "org.jetbrains.dokka")
    dependencies {
        val dokkaVersion: String by project
        configurations["dokkaHtmlPlugin"]("org.jetbrains.dokka:kotlin-as-java-plugin:$dokkaVersion")
    }

    val dokkaJar by tasks.registering(Jar::class) {
        from("$buildDir/dokka/javadoc")
        archiveClassifier.set("javadoc")
        val dokkaJavadoc by tasks
        dependsOn(dokkaJavadoc)
    }

    apply(plugin = "maven-publish")
    publishing {
        publications {
            create<MavenPublication>("mavenJar") {
                afterEvaluate {
                    from(components["java"])
                }
                artifact(sourcesJar)
                artifact(dokkaJar)

                pom {
                    name.set(myproject.name)
                    description.set(myproject.description)
                    url.set(githubUrl)
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
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
                    issueManagement {
                        system.set("GitHub")
                        url.set(githubIssueUrl)
                    }
                    organization {
                        name.set("RADAR-base")
                        url.set("http://radar-base.org")
                    }
                    scm {
                        connection.set("scm:git:$githubUrl")
                        url.set(githubUrl)
                    }
                }
            }
        }
        repositories {
            val nexusRepoBase = "https://repo.thehyve.nl/content/repositories"
            val url = if (myproject.version.toString().endsWith("SNAPSHOT")) "$nexusRepoBase/snapshots" else "$nexusRepoBase/releases"
            maven(url = url) {
                name = "thehyve"
                credentials {
                    username = if (myproject.hasProperty("nexusUser")) myproject.property("nexusUser").toString() else System.getenv("NEXUS_USER")
                    password = if (myproject.hasProperty("nexusPassword")) myproject.property("nexusPassword").toString() else System.getenv("NEXUS_PASSWORD")
                }
            }
            maven(url = "https://api.bintray.com/maven/radar-base/org.radarbase/${myproject.name}/") {
                name = "bintray"
                credentials {
                    username = if (project.hasProperty("bintrayUser")) project.property("bintrayUser").toString() else System.getenv("BINTRAY_USER")
                    password = if (project.hasProperty("bintrayApiKey")) project.property("bintrayApiKey").toString() else System.getenv("BINTRAY_API_KEY")
                }
            }
        }
    }

    afterEvaluate {
        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11"
                apiVersion = "1.4"
                languageVersion = "1.4"
            }
        }

        tasks.withType<Test> {
            testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = true
                exceptionFormat = FULL
            }
            useJUnitPlatform()
        }

        tasks.withType<Tar> {
            compression = Compression.GZIP
            archiveExtension.set("tar.gz")
        }

        tasks.withType<Jar> {
            manifest {
                attributes(
                        "Implementation-Title" to myproject.name,
                        "Implementation-Version" to myproject.version
                )
            }
        }

        tasks.withType<DokkaTask> {
            logging.level = LogLevel.QUIET
        }

        val assemble by tasks
        assemble.dependsOn(sourcesJar)
        assemble.dependsOn(dokkaJar)
    }
}

tasks.wrapper {
    gradleVersion = "6.7"
}
