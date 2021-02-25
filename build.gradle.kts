import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") apply false
    `maven-publish`
    signing
    id("org.jetbrains.dokka") apply false
    id("com.github.ben-manes.versions") version "0.36.0" apply false
}

subprojects {
    val myproject = this
    group = "org.radarbase"
    version = "0.4.4-SNAPSHOT"

    val githubRepoName = "RADAR-base/radar-jersey"
    val githubUrl = "https://github.com/$githubRepoName.git"
    val githubIssueUrl = "https://github.com/$githubRepoName/issues"

    extra.apply {
        set("githubRepoName", githubRepoName)
        set("githubUrl", githubUrl)
        set("githubIssueUrl", githubIssueUrl)
    }

    apply(plugin = "com.github.ben-manes.versions")

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }

    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/radar-cns/org.radarcns")
        maven(url = "https://dl.bintray.com/radar-base/org.radarbase")
        maven(url = "https://repo.thehyve.nl/content/repositories/snapshots")
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
            fun Project.propertyOrEnv(propertyName: String, envName: String): String? {
                return if (hasProperty(propertyName)) {
                    property(propertyName)?.toString()
                } else {
                    System.getenv(envName)
                }
            }

            maven {
                name = "OSSRH"
                credentials {
                    username = propertyOrEnv("ossrh.user", "OSSRH_USER")
                    password = propertyOrEnv("ossrh.password", "OSSRH_PASSWORD")
                }

                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
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

        apply(plugin = "signing")
        signing {
            useGpgCmd()
            isRequired = true
            sign(tasks["sourcesJar"], tasks["dokkaJar"])
            sign(publishing.publications["mavenJar"])
        }
    }
}

tasks.wrapper {
    gradleVersion = "6.8.3"
}
