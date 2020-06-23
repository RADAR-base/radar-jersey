package org.radarbase.jersey.service

class ImmediateHealthService: HealthService {
    private val healthMetrics: MutableList<HealthService.Metric> = ArrayList()

    @Volatile
    override var status: String = "UP"

    override val metrics: Map<String, Any>
        get() {
            val result = mutableMapOf<String, Any>("status" to status)
            synchronized(healthMetrics) {
                healthMetrics.forEach { result[it.name] = it.metrics }
            }
            return result
        }

    override fun add(metric: HealthService.Metric) {
        synchronized(healthMetrics) {
            healthMetrics += metric
        }
    }

    override fun remove(metric: HealthService.Metric) {
        synchronized(healthMetrics) {
            healthMetrics -= metric
        }
    }
}
