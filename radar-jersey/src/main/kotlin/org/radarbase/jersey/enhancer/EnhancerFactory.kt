package org.radarbase.jersey.enhancer

/**
 * Factory to create resource enhancers with.
 */
interface EnhancerFactory {
    fun createEnhancers(): List<JerseyResourceEnhancer>
}
