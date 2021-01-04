plugins {
    kotlin("jvm")
}

description = "Library for Jersey authorization, exception handling and configuration with the RADAR platform"

dependencies {
    val kotlinVersion: String by project
    implementation(kotlin("reflect", version=kotlinVersion))
    api(kotlin("stdlib-jdk8", version=kotlinVersion))

    val managementPortalVersion: String by project
    api("org.radarcns:radar-auth:$managementPortalVersion")

    val jakartaWsRsVersion: String by project
    api("jakarta.ws.rs:jakarta.ws.rs-api:$jakartaWsRsVersion")
    val jakartaAnnotationVersion: String by project
    api("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")
    val hk2Version: String by project
    api("org.glassfish.hk2.external:jakarta.inject:$hk2Version")
    val jerseyVersion: String by project
    api("org.glassfish.jersey.core:jersey-server:$jerseyVersion")

    val jacksonVersion: String by project
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    val okhttpVersion: String by project
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:$jerseyVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    // exception template rendering
    implementation("com.github.spullara.mustache.java:compiler:0.9.6")

    val slf4jVersion: String by project
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    val swaggerVersion: String by project
    implementation("io.swagger.core.v3:swagger-jaxrs2:$swaggerVersion")

    api("org.glassfish.jersey.inject:jersey-hk2:$jerseyVersion")

    runtimeOnly("org.glassfish.jersey.media:jersey-media-json-jackson:$jerseyVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")

    val javaxXmlBindVersion: String by project
    val javaxJaxbCoreVersion: String by project
    val javaxJaxbRuntimeVersion: String by project
    val javaxActivation: String by project
    runtimeOnly("javax.xml.bind:jaxb-api:$javaxXmlBindVersion")
    runtimeOnly("org.glassfish.jaxb:jaxb-core:$javaxJaxbCoreVersion")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:$javaxJaxbRuntimeVersion")
    runtimeOnly("javax.activation:activation:$javaxActivation")

    val grizzlyVersion: String by project
    testRuntimeOnly("org.glassfish.grizzly:grizzly-http-server:$grizzlyVersion")
    testRuntimeOnly("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:$jerseyVersion")

    val junitVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.hamcrest:hamcrest-all:1.3")

    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

tasks.processResources {
    val properties = mapOf("version" to project.version)
    inputs.properties(properties)
    filesMatching(".*/version.properties") {
        expand(properties)
    }
}