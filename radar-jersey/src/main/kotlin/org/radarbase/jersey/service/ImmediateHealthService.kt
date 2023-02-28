package org.radarbase.jersey.service

import jakarta.ws.rs.core.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.glassfish.hk2.api.IterableProvider
import org.radarbase.kotlin.coroutines.forkAny
import org.radarbase.kotlin.coroutines.forkJoin

class ImmediateHealthService(
        @Context healthMetrics: IterableProvider<HealthService.Metric>
): HealthService {
    @Volatile
    private var allMetrics: List<HealthService.Metric> = healthMetrics.toList()

    override suspend fun computeStatus(): HealthService.Status =
        if (allMetrics.forkAny {
            val status = it.computeStatus()
            status == HealthService.Status.DOWN
        }) {
            HealthService.Status.DOWN
        } else {
            HealthService.Status.UP
        }

    override suspend fun computeMetrics(): Map<String, Any> = coroutineScope {
        val metrics = async {
            allMetrics.forkJoin {
                Pair(it.name, it.computeMetrics())
            }
        }
        val status = async {
            computeStatus()
        }
        buildMap {
            put("status", status.await())
            metrics.await().forEach { (name, metric) ->
                put(name, metric)
            }
        }
    }

    override fun add(metric: HealthService.Metric) {
        allMetrics = allMetrics + metric
    }

    override fun remove(metric: HealthService.Metric) {
        allMetrics = allMetrics - metric
    }
}
