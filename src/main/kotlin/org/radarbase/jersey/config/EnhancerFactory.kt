package org.radarbase.jersey.config

import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.filter.CorsFilter
import org.radarbase.jersey.filter.ResponseLoggerFilter

/**
 * Factory to create resource enhancers with.
 */
interface EnhancerFactory {
    fun createEnhancers(): List<JerseyResourceEnhancer>
}