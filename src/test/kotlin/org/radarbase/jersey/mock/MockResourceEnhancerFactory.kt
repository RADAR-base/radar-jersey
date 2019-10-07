package org.radarbase.jersey.mock

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.*

class MockResourceEnhancerFactory(private val config: AuthConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            MockResourceEnhancer(),
            RadarJerseyResourceEnhancer(config),
            ManagementPortalResourceEnhancer(),
            HttpExceptionResourceEnhancer(),
            GeneralExceptionResourceEnhancer())
}