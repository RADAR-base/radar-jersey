package org.radarbase.jersey.hibernate

import org.hibernate.internal.SessionImpl
import org.radarbase.jersey.service.HealthService
import org.radarbase.jersey.service.HealthService.Metric
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.ws.rs.core.Context

class DatabaseHealthMetrics(
        @Context private val entityManager: Provider<EntityManager>
): Metric(name = "db") {
    override val status: HealthService.Status
        get() = try {
            entityManager.get()
                    .unwrap(SessionImpl::class.java)
                    .connection()
            HealthService.Status.UP
        } catch (ex: Throwable) {
            HealthService.Status.DOWN
        }

    override val metrics: Any = mapOf("status" to status)
}
