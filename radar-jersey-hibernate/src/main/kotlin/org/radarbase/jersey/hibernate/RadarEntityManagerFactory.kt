package org.radarbase.jersey.hibernate

import jakarta.ws.rs.core.Context
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.hibernate.internal.SessionImpl
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

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

        fun EntityManager.connection(): Connection = unwrap(SessionImpl::class.java).connection()
    }
}
