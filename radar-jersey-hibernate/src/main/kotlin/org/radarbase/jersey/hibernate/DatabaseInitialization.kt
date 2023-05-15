package org.radarbase.jersey.hibernate

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.ext.Provider
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep.DATABASE_ARG
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
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

        CommandScope(UpdateCommandStep.COMMAND_NAME[0]).run {
            addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, dbConfig.liquibase.changelogs)
            dbConfig.liquibase.contexts.forEach { context ->
                addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, context)
            }
            addArgumentValue(DATABASE_ARG, database)
            execute()
        }
    }

    override fun onRequest(requestEvent: RequestEvent?): RequestEventListener? = null

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseInitialization::class.java)

        @Throws(HibernateException::class)
        fun EntityManager.useConnection(work: (Connection) -> Unit) {
            check(this is Session) { "Cannot use connection of EntityManager that is not a Hibernate Session" }
            doWork(work)
        }
    }
}
