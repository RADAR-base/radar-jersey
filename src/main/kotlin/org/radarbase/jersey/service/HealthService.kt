package org.radarbase.jersey.service

interface HealthService {
    var status: String
    val metrics: Map<String, Any>

    fun add(metric: Metric)
    fun remove(metric: Metric)

    abstract class Metric(val name: String) {
        abstract val metrics: Any

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other?.javaClass != javaClass) return false
            other as Metric
            return name == other.name
        }

        override fun hashCode(): Int = name.hashCode()
    }
}
