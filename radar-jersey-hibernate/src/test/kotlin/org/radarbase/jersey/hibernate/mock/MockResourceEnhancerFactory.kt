package org.radarbase.jersey.hibernate.mock

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

class MockResourceEnhancerFactory(private val config: AuthConfig, private val databaseConfig: DatabaseConfig) :
    EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
        MockResourceEnhancer(),
        Enhancers.radar(config),
        HibernateResourceEnhancer(databaseConfig),
        Enhancers.disabledAuthorization,
        Enhancers.health,
        Enhancers.exception,
    )
}
