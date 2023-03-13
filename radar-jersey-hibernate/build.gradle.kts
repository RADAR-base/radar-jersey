plugins {
    kotlin("jvm")
}

description = "Library for Jersey with Hibernate with the RADAR platform"

dependencies {
    val kotlinVersion: String by project
    implementation(kotlin("reflect", version=kotlinVersion))
    api(kotlin("stdlib-jdk8", version=kotlinVersion))

    api(project(":radar-jersey"))
    val hibernateVersion: String by project
    api("org.hibernate:hibernate-core:$hibernateVersion")
    runtimeOnly("org.hibernate:hibernate-hikaricp:$hibernateVersion")

    val managementPortalVersion: String by project
    implementation("org.radarbase:radar-kotlin:$managementPortalVersion")

    val jakartaValidationVersion: String by project
    runtimeOnly("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
    val hibernateValidatorVersion: String by project
    runtimeOnly("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
    val glassfishJakartaElVersion: String by project
    runtimeOnly("org.glassfish:jakarta.el:$glassfishJakartaElVersion")

    val slf4jVersion: String by project
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    val liquibaseVersion: String by project
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")

    val postgresVersion: String by project
    runtimeOnly("org.postgresql:postgresql:$postgresVersion")

    val grizzlyVersion: String by project
    testRuntimeOnly("org.glassfish.grizzly:grizzly-http-server:$grizzlyVersion")
    val jerseyVersion: String by project
    testRuntimeOnly("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:$jerseyVersion")

    val h2Version: String by project
    testImplementation("com.h2database:h2:$h2Version")

    val junitVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    val hamcrestVersion: String by project
    testImplementation("org.hamcrest:hamcrest:$hamcrestVersion")
    val okhttpVersion: String by project
    testImplementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
}
