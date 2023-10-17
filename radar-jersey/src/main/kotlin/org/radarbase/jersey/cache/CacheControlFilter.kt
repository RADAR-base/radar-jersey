package org.radarbase.jersey.cache

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.core.CacheControl
import jakarta.ws.rs.core.HttpHeaders
import java.io.IOException

/**
 * @author [Bill Burke](mailto:bill@burkecentral.com)
 * @version $Revision: 1 $
 */
@Priority(Priorities.HEADER_DECORATOR)
class CacheControlFilter(
    private val cacheControl: CacheControl,
    private val vary: Array<String>,
) : ContainerResponseFilter {
    @Throws(IOException::class)
    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext,
    ) {
        if (responseContext.status != 200) return

        responseContext.headers.computeIfAbsent(HttpHeaders.CACHE_CONTROL) {
            mutableListOf<Any>(cacheControl)
        }
        if (vary.isNotEmpty()) {
            responseContext.headers.computeIfAbsent(HttpHeaders.VARY) {
                vary.toMutableList<Any>()
            }
        }
    }
}
