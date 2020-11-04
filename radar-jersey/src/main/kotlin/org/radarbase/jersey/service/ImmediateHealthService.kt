package org.radarbase.jersey.service

import org.glassfish.hk2.api.IterableProvider
import javax.ws.rs.core.Context

class ImmediateHealthService(
        @Context healthMetrics: IterableProvider<HealthService.Metric>
): HealthService {
    @Volatile
    private var allMetrics: List<HealthService.Metric> = healthMetrics.toList()

    override val status: HealthService.Status
        get() = if (allMetrics.any { it.status == HealthService.Status.DOWN }) {
            HealthService.Status.DOWN
        } else {
            HealthService.Status.UP
        }

    override val metrics: Map<String, Any>
        get() {
            val metrics = allMetrics
            val result = mutableMapOf<String, Any>(
                    "status" to if (metrics.any { it.status == HealthService.Status.DOWN }) {
                        HealthService.Status.DOWN
                    } else {
                        HealthService.Status.UP
                    })
            metrics.forEach  { result[it.name] = it.metrics }
            return result
        }

    override fun add(metric: HealthService.Metric) {
        allMetrics = allMetrics + metric
    }

    override fun remove(metric: HealthService.Metric) {
        allMetrics = allMetrics - metric
    }
}
