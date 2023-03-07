package org.radarbase.jersey.hibernate

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.glassfish.jersey.process.internal.RequestScope
import org.radarbase.jersey.hibernate.DatabaseInitialization.Companion.useConnection
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.HealthService.Metric
import org.radarbase.kotlin.coroutines.CacheConfig
import org.radarbase.kotlin.coroutines.CachedValue
import kotlin.time.Duration.Companion.seconds

class DatabaseHealthMetrics(
    @Context private val entityManager: Provider<EntityManager>,
    @Context dbConfig: DatabaseConfig,
    @Context private val requestScope: RequestScope,
): Metric(name = "db") {
    private val cachedStatus = CachedValue(
        CacheConfig(
            refreshDuration = dbConfig.healthCheckValiditySeconds.seconds,
            retryDuration = dbConfig.healthCheckValiditySeconds.seconds,
        ),
        ::testConnection,
    )

    override suspend fun computeStatus(): HealthService.Status =
        cachedStatus.get { it == HealthService.Status.UP }.value

    override suspend fun computeMetrics(): Map<String, Any> = mapOf("status" to computeStatus())

    private suspend fun testConnection(): HealthService.Status = withContext(Dispatchers.IO) {
        try {
            requestScope.runInScope {
                entityManager.get().useConnection { it.close() }
            }
            HealthService.Status.UP
        } catch (ex: Throwable) {
            HealthService.Status.DOWN
        }
    }
}
