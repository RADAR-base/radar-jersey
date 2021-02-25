package org.radarbase.jersey.hibernate

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import org.radarbase.jersey.hibernate.RadarEntityManagerFactory.Companion.connection
import org.radarbase.jersey.hibernate.RadarEntityManagerFactoryFactory.Companion.useEntityManager
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.slf4j.LoggerFactory
import java.sql.Connection
import javax.persistence.EntityManagerFactory
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.ext.Provider

@Provider
class DatabaseInitialization(
        @Context private val entityManagerFactory: jakarta.inject.Provider<EntityManagerFactory>,
        @Context private val dbConfig: DatabaseConfig,
) : ApplicationEventListener {
    override fun onEvent(event: ApplicationEvent) {
        logger.info("Application state: {}", event.type)
        if (event.type != ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) return
        try {
            entityManagerFactory.get().useEntityManager {
                    // make first connection
                    it.connection().use { connection ->
                        if (dbConfig.liquibase.enable) {
                            initializeLiquibase(connection)
                        }
                    }
            }
        } catch (ex: Throwable) {
            throw IllegalStateException("Cannot initialize database.", ex)
        }
    }

    private fun initializeLiquibase(connection: Connection) {
        logger.info("Initializing Liquibase")
        val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                        JdbcConnection(connection))
        Liquibase(dbConfig.liquibase.changelogs, ClassLoaderResourceAccessor(), database).use {
            it.update(null as Contexts?)
        }
    }

    override fun onRequest(requestEvent: RequestEvent?): RequestEventListener? = null

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseInitialization::class.java)
    }
}
