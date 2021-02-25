package org.radarbase.jersey.mock

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.auth.RadarJerseyResourceEnhancerTest
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.service.ProjectService
import org.radarcns.auth.authentication.TokenValidator
import jakarta.inject.Singleton

class MockResourceEnhancer : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            ConfigLoader.Filters.logResponse)

    override val packages: Array<String> = arrayOf(
            "org.radarbase.jersey.mock.resource")

    override fun AbstractBinder.enhance() {
        bind(MockProjectService(listOf("a", "b")))
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

        bindFactory { RadarJerseyResourceEnhancerTest.oauthHelper.tokenValidator }
                .to(TokenValidator::class.java)
    }
}
