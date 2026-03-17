plugins {
    kotlin("jvm")
}

description = "Library for Jersey authorization, exception handling and configuration with the RADAR platform"

dependencies {
    implementation(libs.kotlin.reflect)
    api(libs.kotlin.stdlib)

    api(libs.radar.auth)
    api(libs.radar.mp.client)
    implementation(libs.radar.commons.kotlin)

    implementation(libs.java.jwt)

    api(libs.jakarta.ws.rs.api)
    api(libs.jakarta.annotation.api)
    api(libs.hk2)

    api(libs.jersey.hk2.inject)
    api(libs.jersey.server)
    implementation(libs.jersey.media.json)

    // Using the BOM and the Bundle
    api(platform(libs.jackson.bom))
    implementation(libs.bundles.jackson)

    implementation(libs.jersey.container.grizzly)
    implementation(libs.mustache)

    api(libs.swagger.annotations)
    api(libs.swagger.models)
    implementation(libs.swagger.jaxrs2) {
        exclude(group = "com.fasterxml.jackson.jaxrs", module = "jackson-jaxrs-json-provider")
    }
    runtimeOnly(libs.jakarta.servlet.api)

    runtimeOnly(libs.jakarta.xml.bind.api)
    runtimeOnly(libs.bundles.jaxb.runtime)
    runtimeOnly(libs.jakarta.activation.api)

    testImplementation(libs.okhttp)
    testRuntimeOnly(libs.grizzly.http.server)
    testRuntimeOnly(libs.jersey.test.container.grizzly)
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.kotlin)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.processResources {
    val properties = mapOf("version" to project.version)
    inputs.properties(properties)
    filesMatching(".*/version.properties") {
        expand(properties)
    }
}
