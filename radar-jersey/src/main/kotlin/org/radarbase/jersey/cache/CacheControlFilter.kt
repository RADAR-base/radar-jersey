package org.radarbase.jersey.cache

import java.io.IOException
import javax.annotation.Priority
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.core.CacheControl
import javax.ws.rs.core.HttpHeaders

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
