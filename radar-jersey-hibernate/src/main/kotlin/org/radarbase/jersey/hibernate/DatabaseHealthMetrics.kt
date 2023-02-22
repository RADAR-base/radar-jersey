package org.radarbase.jersey.hibernate

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.hibernate.DatabaseInitialization.Companion.useConnection
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.HealthService.Metric
import org.radarbase.jersey.util.CacheConfig
import org.radarbase.jersey.util.CachedValue
import org.slf4j.LoggerFactory
import java.time.Duration

class DatabaseHealthMetrics(
        @Context private val entityManager: Provider<EntityManager>,
        @Context dbConfig: DatabaseConfig
): Metric(name = "db") {
    private val cachedStatus = CachedValue(
        CacheConfig(
            refreshDuration = Duration.ofSeconds(dbConfig.healthCheckValiditySeconds),
            retryDuration = Duration.ofSeconds(dbConfig.healthCheckValiditySeconds),
        ),
        ::testConnection,
    )

    override fun computeStatus(): HealthService.Status =
        cachedStatus.get { it == HealthService.Status.UP }
            .also { logger.info("Returning status {}", it) }

    override fun computeMetrics(): Map<String, Any> = mapOf("status" to computeStatus())

    private fun testConnection(): HealthService.Status = try {
        entityManager.get().useConnection { }
        logger.info("Database UP")
        HealthService.Status.UP
    } catch (ex: Throwable) {
        logger.info("Database DOWN")
        HealthService.Status.DOWN
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseHealthMetrics::class.java)
    }
}
