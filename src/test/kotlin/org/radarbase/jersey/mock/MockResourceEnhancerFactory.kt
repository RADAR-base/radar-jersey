package org.radarbase.jersey.mock

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.EnhancerFactory
import org.radarbase.jersey.config.JerseyResourceEnhancer

class MockResourceEnhancerFactory(private val config: AuthConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            MockResourceEnhancer(),
            ConfigLoader.Enhancers.radar(config),
            ConfigLoader.Enhancers.managementPortal(config),
            ConfigLoader.Enhancers.httpException,
            ConfigLoader.Enhancers.generalException)
}
