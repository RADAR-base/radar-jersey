package org.radarbase.jersey.mock

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class MockDisabledAuthResourceEnhancerFactory(private val config: AuthConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            MockResourceEnhancer(),
            Enhancers.radar(config),
            Enhancers.disabledAuthorization,
            Enhancers.exception,
    )
}
