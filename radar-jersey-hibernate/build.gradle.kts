plugins {
    kotlin("jvm")
}

description = "Library for Jersey with Hibernate with the RADAR platform"

dependencies {

    /* The entries in the block below are added here to force the version of
     * transitive dependencies and mitigate reported vulnerabilities
     */
    implementation("org.apache.commons:commons-lang3:3.18.0")

    implementation(kotlin("reflect", version = Versions.kotlin))
    api(kotlin("stdlib-jdk8", version = Versions.kotlin))

    api(project(":radar-jersey"))
    api("org.hibernate:hibernate-core:${Versions.hibernate}")
    runtimeOnly("org.hibernate:hibernate-hikaricp:${Versions.hibernate}")

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    runtimeOnly("jakarta.validation:jakarta.validation-api:${Versions.jakartaValidation}")
    runtimeOnly("org.hibernate.validator:hibernate-validator:${Versions.hibernateValidator}")

    runtimeOnly("org.glassfish:jakarta.el:${Versions.glassfishJakartaEl}")

    implementation("org.liquibase:liquibase-core:${Versions.liquibase}")

    runtimeOnly("org.postgresql:postgresql:${Versions.postgres}")

    testRuntimeOnly("org.glassfish.grizzly:grizzly-http-server:${Versions.grizzly}")
    testRuntimeOnly("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${Versions.jersey}")

    testImplementation("com.h2database:h2:${Versions.h2}")

    testImplementation("org.hamcrest:hamcrest:${Versions.hamcrest}")
    testImplementation("com.squareup.okhttp3:okhttp:${Versions.okhttp}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
