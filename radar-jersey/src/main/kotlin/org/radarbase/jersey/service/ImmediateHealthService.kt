package org.radarbase.jersey.service

import jakarta.ws.rs.core.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.glassfish.hk2.api.IterableProvider
import org.radarbase.jersey.util.concurrentAny
import org.radarbase.jersey.util.forkJoin
import org.slf4j.LoggerFactory

class ImmediateHealthService(
        @Context healthMetrics: IterableProvider<HealthService.Metric>
): HealthService {
    @Volatile
    private var allMetrics: List<HealthService.Metric> = healthMetrics.toList()

    override fun computeStatus(): HealthService.Status =
        if (allMetrics.any {
            val status = it.computeStatus()
            logger.info("Returning status {} from metric {}", status, it.name)
            status == HealthService.Status.DOWN
        }) {
            HealthService.Status.DOWN
        } else {
            HealthService.Status.UP
        }

    override fun computeMetrics(): Map<String, Any> = buildMap {
        put("status", computeStatus())
        allMetrics.forEach { metric ->
            put(metric.name, metric.computeMetrics())
        }
    }

    override fun add(metric: HealthService.Metric) {
        allMetrics = allMetrics + metric
    }

    override fun remove(metric: HealthService.Metric) {
        allMetrics = allMetrics - metric
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ImmediateHealthService::class.java)
    }
}
