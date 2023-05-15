package org.radarbase.jersey.hibernate

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.ws.rs.core.Context
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.slf4j.LoggerFactory

class RadarEntityManagerFactory(
    @Context private val emf: EntityManagerFactory,
) : DisposableSupplier<EntityManager> {

    override fun get(): EntityManager {
        logger.debug("Creating EntityManager...")
        return emf.createEntityManager()
    }

    override fun dispose(instance: EntityManager?) {
        instance?.let {
            logger.debug("Disposing EntityManager")
            it.close()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RadarEntityManagerFactory::class.java)
    }
}
