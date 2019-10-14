package org.radarbase.jersey.mock

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.auth.ProjectService
import org.radarbase.jersey.auth.RadarJerseyResourceEnhancerTest
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarcns.auth.authentication.TokenValidator
import javax.inject.Singleton

class MockResourceEnhancer : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            ConfigLoader.Filters.logResponse)

    override val packages: Array<String> = arrayOf(
            "org.radarbase.jersey.mock.resource")

    override fun enhanceBinder(binder: AbstractBinder) {
        binder.bind(MockProjectService(listOf("a", "b")))
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)

        binder.bindFactory { RadarJerseyResourceEnhancerTest.oauthHelper.tokenValidator }
                .to(TokenValidator::class.java)
    }
}