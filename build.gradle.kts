import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    `maven-publish`
    signing
    id("org.jetbrains.dokka") apply false
    id("com.github.ben-manes.versions") version "0.38.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

allprojects {
    group = "org.radarbase"
    version = "0.6.1"

    afterEvaluate {
        tasks.withType<DependencyUpdatesTask> {
            rejectVersionIf {
                isNonStable(candidate.version)
            }
        }
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "org.jetbrains.dokka")

    val myproject = this

    val githubRepoName = "RADAR-base/radar-jersey"
    val githubUrl = "https://github.com/$githubRepoName.git"
    val githubIssueUrl = "https://github.com/$githubRepoName/issues"

    extra.apply {
        set("githubRepoName", githubRepoName)
        set("githubUrl", githubUrl)
        set("githubIssueUrl", githubIssueUrl)
    }

    repositories {
        mavenCentral()
        // Temporary until Dokka is fully published on maven central.
        // https://github.com/Kotlin/kotlinx.html/issues/81
        maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    }

    dependencies {
        val dokkaVersion: String by project
        configurations["dokkaHtmlPlugin"]("org.jetbrains.dokka:kotlin-as-java-plugin:$dokkaVersion")

        val log4j2Version: String by project
        val testRuntimeOnly by configurations
        testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-api:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")
    }

    val sourcesJar by tasks.registering(Jar::class) {
        from(myproject.the<SourceSetContainer>()["main"].allSource)
        archiveClassifier.set("sources")
        val classes by tasks
        dependsOn(classes)
    }

    val dokkaJar by tasks.registering(Jar::class) {
        from("$buildDir/dokka/javadoc")
        archiveClassifier.set("javadoc")
        val dokkaJavadoc by tasks
        dependsOn(dokkaJavadoc)
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
            systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
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

        val assemble by tasks
        assemble.dependsOn(sourcesJar)
        assemble.dependsOn(dokkaJar)

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
        }

        signing {
            useGpgCmd()
            isRequired = true
            sign(tasks["sourcesJar"], tasks["dokkaJar"])
            sign(publishing.publications["mavenJar"])
        }

        tasks.withType<Sign>().configureEach {
            onlyIf { gradle.taskGraph.hasTask("${project.path}:publish") }
        }
    }
}

fun Project.propertyOrEnv(propertyName: String, envName: String): String? {
    return if (hasProperty(propertyName)) {
        property(propertyName)?.toString()
    } else {
        System.getenv(envName)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(propertyOrEnv("ossrh.user", "OSSRH_USER"))
            password.set(propertyOrEnv("ossrh.password", "OSSRH_PASSWORD"))
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.0.2"
}
