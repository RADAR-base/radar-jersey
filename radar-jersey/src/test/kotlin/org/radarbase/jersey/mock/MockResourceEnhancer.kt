package org.radarbase.jersey.mock

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.jersey.auth.OAuthHelper
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.service.ProjectService

class MockResourceEnhancer : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            ConfigLoader.Filters.logResponse)

    override val packages: Array<String> = arrayOf(
            "org.radarbase.jersey.mock.resource")

    override fun AbstractBinder.enhance() {
        bind(MockProjectService(listOf("a", "b")))
            .to(ProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(OAuthHelper().tokenValidator)
            .to(TokenValidator::class.java)
    }
}
