package org.radarbase.jersey.hibernate

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.ws.rs.core.Context
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.hibernate.jpa.HibernatePersistenceProvider
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.RadarPersistenceInfo
import org.slf4j.LoggerFactory
import java.util.Properties

/**
 * Creates EntityManagerFactory using Hibernate. When an [EntityManagerFactory] is created,
 * Liquibase is used to initialize the database, if so configured.
 */
class RadarEntityManagerFactoryFactory(
    @Context config: DatabaseConfig,
) : DisposableSupplier<EntityManagerFactory> {
    private val persistenceInfo = RadarPersistenceInfo(config)
    private val persistenceProvider = HibernatePersistenceProvider()

    override fun get(): EntityManagerFactory {
        logger.info("Initializing EntityManagerFactory with config: $persistenceInfo")

        return persistenceProvider.createContainerEntityManagerFactory(persistenceInfo, Properties())
    }

    override fun dispose(instance: EntityManagerFactory?) {
        logger.info("Disposing EntityManagerFactory")
        instance?.close()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RadarEntityManagerFactoryFactory::class.java)
    }
}

/**
 * Use an EntityManager for the duration of [method]. No reference of the passed
 * [EntityManager] should be returned, either directly or indirectly.
 */
@Suppress("unused")
inline fun <T> EntityManagerFactory.useEntityManager(method: (EntityManager) -> T): T {
    return createEntityManager().use { entityManager ->
        method(entityManager)
    }
}
