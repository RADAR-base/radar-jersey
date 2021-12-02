package org.radarbase.jersey.filter

import org.radarbase.jersey.cache.CacheControlFeature

object Filters {
    /** Adds CORS headers to all responses. */
    val cors = CorsFilter::class.java
    /** Log the HTTP status responses of all requests. */
    val logResponse = ResponseLoggerFilter::class.java
    /** Add cache control headers to responses. */
    val cache = CacheControlFeature::class.java
}
