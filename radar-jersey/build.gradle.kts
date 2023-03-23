plugins {
    kotlin("jvm")
}

description = "Library for Jersey authorization, exception handling and configuration with the RADAR platform"

dependencies {
    implementation(kotlin("reflect", version = Versions.kotlin))
    api(kotlin("stdlib-jdk8", version = Versions.kotlin))

    api("org.radarbase:radar-auth:${Versions.managementPortal}")
    api("org.radarbase:managementportal-client:${Versions.managementPortal}")

    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    implementation("com.auth0:java-jwt:${Versions.javaJwt}")

    api("jakarta.ws.rs:jakarta.ws.rs-api:${Versions.jakartaWsRs}")
    api("jakarta.annotation:jakarta.annotation-api:${Versions.jakartaAnnotation}")
    api("org.glassfish.hk2:hk2:${Versions.hk2}")

    api("org.glassfish.jersey.inject:jersey-hk2:${Versions.jersey}")
    api("org.glassfish.jersey.core:jersey-server:${Versions.jersey}")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:${Versions.jersey}")

    api(platform("com.fasterxml.jackson:jackson-bom:${Versions.jackson}"))
    api("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation("com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider")

    implementation("org.glassfish.jersey.containers:jersey-container-grizzly2-http:${Versions.jersey}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    // exception template rendering
    implementation("com.github.spullara.mustache.java:compiler:${Versions.mustache}")

    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")

    implementation("io.swagger.core.v3:swagger-jaxrs2-jakarta:${Versions.swagger}") {
        exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
    }

    runtimeOnly("jakarta.xml.bind:jakarta.xml.bind-api:${Versions.jakartaXmlBind}")
    runtimeOnly("org.glassfish.jaxb:jaxb-core:${Versions.jakartaJaxbCore}")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:${Versions.jakartaJaxbRuntime}")
    runtimeOnly("jakarta.activation:jakarta.activation-api:${Versions.jakartaActivation}")

    testImplementation("com.squareup.okhttp3:okhttp:${Versions.okhttp}")

    testRuntimeOnly("org.glassfish.grizzly:grizzly-http-server:${Versions.grizzly}")
    testRuntimeOnly("org.glassfish.jersey.containers:jersey-container-grizzly2-servlet:${Versions.jersey}")

    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
    testImplementation("org.hamcrest:hamcrest:${Versions.hamcrest}")

    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
}

tasks.processResources {
    val properties = mapOf("version" to project.version)
    inputs.properties(properties)
    filesMatching(".*/version.properties") {
        expand(properties)
    }
}
