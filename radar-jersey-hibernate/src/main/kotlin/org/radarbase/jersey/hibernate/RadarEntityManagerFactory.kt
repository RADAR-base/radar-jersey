package org.radarbase.jersey.hibernate

import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.internal.SessionImpl
import org.slf4j.LoggerFactory
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.ws.rs.core.Context

class RadarEntityManagerFactory(
        @Context private val emf: EntityManagerFactory
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

        fun EntityManager.connection() = unwrap(SessionImpl::class.java).connection()
    }
}
