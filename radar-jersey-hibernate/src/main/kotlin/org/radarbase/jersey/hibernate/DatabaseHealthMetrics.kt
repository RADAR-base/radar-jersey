package org.radarbase.jersey.hibernate

import org.hibernate.internal.SessionImpl
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.HealthService.Metric
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.ws.rs.core.Context

class DatabaseHealthMetrics(
        @Context private val entityManager: Provider<EntityManager>
): Metric(name = "db") {
    override val status: HealthService.Status
        get() = if (checkConnection()) HealthService.Status.UP else HealthService.Status.DOWN

    private val isTestingConnection = AtomicBoolean(false)

    @Volatile
    private var previousTestResult = false

    override val metrics: Any = mapOf("status" to status)

    private fun checkConnection(): Boolean {
        return if (isTestingConnection.compareAndSet(false, true)) {
            testConnection()
                    .also {
                        previousTestResult = it
                        isTestingConnection.set(false)
                    }
        } else {
            previousTestResult
        }
    }

    private fun testConnection(): Boolean = try {
        entityManager.get()
                .unwrap(SessionImpl::class.java)
                .connection()
        true
    } catch (ex: Throwable) {
        false
    }
}
