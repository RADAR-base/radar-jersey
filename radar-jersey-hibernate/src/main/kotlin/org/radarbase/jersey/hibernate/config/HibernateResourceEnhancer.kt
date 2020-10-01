package org.radarbase.jersey.hibernate.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.DatabaseHealthMetrics
import org.radarbase.jersey.hibernate.DatabaseInitialization
import org.radarbase.jersey.hibernate.RadarEntityManagerFactory
import org.radarbase.jersey.hibernate.RadarEntityManagerFactoryFactory
import org.radarbase.jersey.service.HealthService
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

class HibernateResourceEnhancer(
        private val databaseConfig: DatabaseConfig
) : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(DatabaseInitialization::class.java)

    override fun AbstractBinder.enhance() {
        bind(databaseConfig.combineWithEnv())
                .to(DatabaseConfig::class.java)

        bind(DatabaseHealthMetrics::class.java)
                .named("db")
                .to(HealthService.Metric::class.java)
                .`in`(Singleton::class.java)

        bindFactory(RadarEntityManagerFactoryFactory::class.java)
                .to(EntityManagerFactory::class.java)
                .`in`(Singleton::class.java)

        bindFactory(RadarEntityManagerFactory::class.java)
                .to(EntityManager::class.java)
                .`in`(RequestScoped::class.java)
    }
}
