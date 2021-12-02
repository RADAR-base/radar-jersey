package org.radarbase.jersey.filter.cache

import java.io.IOException
import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.core.CacheControl
import jakarta.ws.rs.core.HttpHeaders

/**
 * @author [Bill Burke](mailto:bill@burkecentral.com)
 * @version $Revision: 1 $
 */
@Priority(Priorities.HEADER_DECORATOR)
class CacheControlFilter(
    private var cacheControl: CacheControl
) : ContainerResponseFilter {
    @Throws(IOException::class)
    override fun filter(
        requestContext: ContainerRequestContext,
        responseContext: ContainerResponseContext
    ) {
        if (responseContext.status == 200
            && !responseContext.headers.containsKey(HttpHeaders.CACHE_CONTROL)
        ) {
            responseContext.headers[HttpHeaders.CACHE_CONTROL] = mutableListOf<Any>(cacheControl)
        }
    }
}
