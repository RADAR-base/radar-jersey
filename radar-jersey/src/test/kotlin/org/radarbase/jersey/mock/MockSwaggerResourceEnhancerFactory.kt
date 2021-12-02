package org.radarbase.jersey.mock

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import java.util.*

class MockSwaggerResourceEnhancerFactory(private val config: AuthConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val properties = MockSwaggerResourceEnhancerFactory::class.java.getResourceAsStream("/version.properties").use {
            Properties().apply { load(it) }
        }
        return listOf(
            MockResourceEnhancer(),
            Enhancers.radar(config),
            Enhancers.disabledAuthorization,
            Enhancers.swagger(OpenAPI().apply {
                info = Info().apply {
                    version = properties.getProperty("version")
                    description = "MockProject"
                    license = License().apply {
                        name = "Apache-2.0"
                    }
                }
            }, setOf(
                "/health",
            )),
        )
    }
}
