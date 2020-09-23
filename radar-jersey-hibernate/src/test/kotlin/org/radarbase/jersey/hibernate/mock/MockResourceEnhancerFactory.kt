package org.radarbase.jersey.hibernate.mock

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.EnhancerFactory
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

class MockResourceEnhancerFactory(private val config: AuthConfig, private val databaseConfig: DatabaseConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            MockResourceEnhancer(),
            ConfigLoader.Enhancers.radar(config),
            HibernateResourceEnhancer(databaseConfig),
            ConfigLoader.Enhancers.disabledAuthorization,
            ConfigLoader.Enhancers.health,
            ConfigLoader.Enhancers.httpException,
            ConfigLoader.Enhancers.generalException)
}
