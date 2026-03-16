plugins {
    kotlin("jvm")
}

description = "Library for Jersey with Hibernate with the RADAR platform"

dependencies {
    implementation(libs.commons.lang3)

    implementation(libs.kotlin.reflect)
    api(libs.kotlin.stdlib)

    api(project(":radar-jersey"))

    api(libs.hibernate.core)
    runtimeOnly(libs.hibernate.hikaricp)
    implementation(libs.radar.commons.kotlin)

    runtimeOnly(libs.jakarta.validation.api)
    runtimeOnly(libs.hibernate.validator)
    runtimeOnly(libs.glassfish.jakarta.el)

    implementation(libs.liquibase.core)
    runtimeOnly(libs.postgresql)

    testRuntimeOnly(libs.grizzly.http.server)
    testRuntimeOnly(libs.jersey.test.container.grizzly)
    testImplementation(libs.h2)
    testImplementation(libs.hamcrest)
    testImplementation(libs.okhttp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
