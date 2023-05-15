package org.radarbase.jersey.service

interface HealthService {
    fun add(metric: Metric)
    fun remove(metric: Metric)

    suspend fun computeStatus(): Status
    suspend fun computeMetrics(): Map<String, Any>

    abstract class Metric(
        val name: String,
    ) {
        abstract suspend fun computeStatus(): Status?
        abstract suspend fun computeMetrics(): Map<String, Any>

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other?.javaClass != javaClass) return false
            other as Metric
            return name == other.name
        }

        override fun hashCode(): Int = name.hashCode()
    }

    enum class Status {
        UP, DOWN
    }
}
