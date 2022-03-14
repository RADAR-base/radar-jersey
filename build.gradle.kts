import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    `maven-publish`
    signing
    id("org.jetbrains.dokka") apply false
    id("com.github.ben-manes.versions") version "0.42.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

allprojects {
    group = "org.radarbase"
    version = "0.8.2"
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")

    val myProject = this

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
    }

    dependencies {
        val dokkaVersion: String by project
        configurations["dokkaHtmlPlugin"]("org.jetbrains.dokka:kotlin-as-java-plugin:$dokkaVersion")

        val jacksonVersion: String by project
        configurations["dokkaPlugin"](platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
        configurations["dokkaRuntime"](platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))

        val jsoupVersion: String by project
        configurations["dokkaPlugin"]("org.jsoup:jsoup:$jsoupVersion")
        configurations["dokkaRuntime"]("org.jsoup:jsoup:$jsoupVersion")

        val log4j2Version: String by project
        val testRuntimeOnly by configurations
        testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-api:$log4j2Version")
        testRuntimeOnly("org.apache.logging.log4j:log4j-jul:$log4j2Version")
    }

    val sourcesJar by tasks.registering(Jar::class) {
        from(myProject.the<SourceSetContainer>()["main"].allSource)
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

    tasks.withType<JavaCompile> {
        options.release.set(11)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            apiVersion = "1.6"
            languageVersion = "1.6"
        }
    }

    afterEvaluate {
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
                    "Implementation-Title" to myProject.name,
                    "Implementation-Version" to myProject.version
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
                        name.set(myProject.name)
                        description.set(myProject.description)
                        url.set(githubUrl)
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
                            url.set("https://radar-base.org")
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

val stableVersionRegex = "[0-9,.v-]+(-r)?".toRegex()

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA")
        .any { version.toUpperCase().contains(it) }
    return !stableKeyword && !stableVersionRegex.matches(version)
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
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
    gradleVersion = "7.4.1"
}
