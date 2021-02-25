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
    runtimeOnly("org.hibernate:hibernate-c3p0:$hibernateVersion")

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
    testImplementation("org.hamcrest:hamcrest-all:1.3")
    val okhttpVersion: String by project
    testImplementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}
