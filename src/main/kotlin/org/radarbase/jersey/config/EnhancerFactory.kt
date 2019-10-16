package org.radarbase.jersey.config

/**
 * Factory to create resource enhancers with.
 */
interface EnhancerFactory {
    fun createEnhancers(): List<JerseyResourceEnhancer>
}