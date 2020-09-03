package org.radarbase.jersey.hibernate

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import org.glassfish.jersey.internal.inject.DisposableSupplier
import org.hibernate.internal.SessionImpl
import org.hibernate.jpa.HibernatePersistenceProvider
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.RadarPersistenceInfo
import org.slf4j.LoggerFactory
import java.util.*
import javax.persistence.EntityManagerFactory
import javax.ws.rs.core.Context

class RadarEntityManagerFactoryFactory(
        @Context config: DatabaseConfig
) : DisposableSupplier<EntityManagerFactory> {
    private val persistenceInfo = RadarPersistenceInfo(config)
    private val persistenceProvider = HibernatePersistenceProvider()
    private val liquibaseConfig = config.liquibase

    override fun get(): EntityManagerFactory {
        logger.info("Initializing EntityManagerFactory with config: $persistenceInfo")

        return persistenceProvider.createContainerEntityManagerFactory(persistenceInfo, Properties())
                .also { initializeDatabase(it) }
    }

    private fun initializeDatabase(emf: EntityManagerFactory) {
        if (!liquibaseConfig.enable) return

        logger.info("Initializing Liquibase")
        try {
            val database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(
                            JdbcConnection(emf.createEntityManager()
                                    .unwrap(SessionImpl::class.java)
                                    .connection()))
            val liquibase = Liquibase(liquibaseConfig.changelogs, ClassLoaderResourceAccessor(), database)
            liquibase.update(null as Contexts?)
        } catch (e: Throwable) {
            logger.error("Failed to initialize database", e)
        }
    }

    override fun dispose(instance: EntityManagerFactory?) {
        logger.info("Disposing EntityManagerFactory")
        instance?.close()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RadarEntityManagerFactoryFactory::class.java)
    }
}

