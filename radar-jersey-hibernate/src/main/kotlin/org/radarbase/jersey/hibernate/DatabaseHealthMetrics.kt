package org.radarbase.jersey.hibernate

import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import org.radarbase.jersey.hibernate.RadarEntityManagerFactory.Companion.connection
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.HealthService.Metric
import org.radarbase.jersey.util.CachedValue
import org.slf4j.LoggerFactory
import java.time.Duration
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.ws.rs.core.Context

class DatabaseHealthMetrics(
        @Context private val entityManager: Provider<EntityManager>,
        @Context dbConfig: DatabaseConfig
): Metric(name = "db") {
    private val cachedStatus = CachedValue(
            Duration.ofSeconds(dbConfig.healthCheckValiditySeconds),
            Duration.ofSeconds(dbConfig.healthCheckValiditySeconds)) {
        testConnection()
    }

    override val status: HealthService.Status
        get() = cachedStatus.get { it == HealthService.Status.UP }

    override val metrics: Any
        get() = mapOf("status" to status)

    private fun testConnection(): HealthService.Status = try {
        entityManager.get().connection().close()
        HealthService.Status.UP
    } catch (ex: Throwable) {
        HealthService.Status.DOWN
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseHealthMetrics::class.java)
    }
}
