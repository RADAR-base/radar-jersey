package org.radarbase.jersey.hibernate

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.ext.Provider
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.glassfish.jersey.server.monitoring.ApplicationEvent
import org.glassfish.jersey.server.monitoring.ApplicationEventListener
import org.glassfish.jersey.server.monitoring.RequestEvent
import org.glassfish.jersey.server.monitoring.RequestEventListener
import org.hibernate.HibernateException
import org.hibernate.Session
import org.radarbase.jersey.hibernate.RadarEntityManagerFactoryFactory.Companion.useEntityManager
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.slf4j.LoggerFactory
import java.sql.Connection

@Provider
class DatabaseInitialization(
    @Context private val entityManagerFactory: jakarta.inject.Provider<EntityManagerFactory>,
    @Context private val dbConfig: DatabaseConfig,
) : ApplicationEventListener {

    override fun onEvent(event: ApplicationEvent) {
        if (event.type != ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) return
        try {
            entityManagerFactory.get().useEntityManager { em ->
                em.useConnection { connection ->
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
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        Liquibase(
            dbConfig.liquibase.changelogs,
            ClassLoaderResourceAccessor(),
            database,
        ).use { it.update(Contexts(dbConfig.liquibase.contexts)) }
    }

    override fun onRequest(requestEvent: RequestEvent?): RequestEventListener? = null

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseInitialization::class.java)

        @Throws(HibernateException::class)
        fun EntityManager.useConnection(work: (Connection) -> Unit) {
            check(this is Session) { "Cannot use connection of EntityManager that is not a Hibernate Session" }
            doWork { connection ->
                work(connection)
                connection.close()
            }
        }
    }
}
