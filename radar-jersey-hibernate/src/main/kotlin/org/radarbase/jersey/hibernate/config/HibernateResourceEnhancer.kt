package org.radarbase.jersey.hibernate.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.process.internal.RequestScoped
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.RadarEntityManagerFactory
import org.radarbase.jersey.hibernate.RadarEntityManagerFactoryFactory
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

class HibernateResourceEnhancer(
        private val databaseConfig: DatabaseConfig
) : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        bind(databaseConfig)
                .to(DatabaseConfig::class.java)

        bindFactory(RadarEntityManagerFactoryFactory::class.java)
                .to(EntityManagerFactory::class.java)
                .`in`(Singleton::class.java)

        bindFactory(RadarEntityManagerFactory::class.java)
                .to(EntityManager::class.java)
                .`in`(RequestScoped::class.java)
    }
}
