package org.radarbase.jersey.service

interface HealthService {
    fun add(metric: Metric)
    fun remove(metric: Metric)

    fun computeStatus(): Status
    fun computeMetrics(): Map<String, Any>

    abstract class Metric(
        val name: String,
    ) {
        abstract fun computeStatus(): Status?
        abstract fun computeMetrics(): Map<String, Any>

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
