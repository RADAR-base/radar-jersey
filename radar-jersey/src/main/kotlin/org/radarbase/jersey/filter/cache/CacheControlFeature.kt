package org.radarbase.jersey.filter.cache

import java.lang.reflect.AnnotatedElement
import jakarta.ws.rs.GET
import jakarta.ws.rs.container.DynamicFeature
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.CacheControl
import jakarta.ws.rs.core.FeatureContext

/**
 * @author [Bill Burke](mailto:bill@burkecentral.com)
 * @version $Revision: 1 $
 */
class CacheControlFeature : DynamicFeature {
    override fun configure(resourceInfo: ResourceInfo, configurable: FeatureContext) {
        val method = resourceInfo.resourceMethod ?: return
        if (!method.isAnnotationPresent(GET::class.java)) return
        if (method.registerCacheControl(configurable)) return
        if (method.registerNoCacheControl(configurable)) return

        val resource = resourceInfo.resourceClass ?: return
        if (resource.registerCacheControl(configurable)) return
        if (resource.registerNoCacheControl(configurable)) return
    }

    private fun AnnotatedElement.registerCacheControl(configurable: FeatureContext): Boolean {
        val cache = getAnnotation(Cache::class.java) ?: return false
        val cacheControl = CacheControl().apply {
            if (cache.isPrivate) {
                isPrivate = true
            }
            if (cache.maxAge > -1) {
                maxAge = cache.maxAge
            }
            if (cache.sMaxAge > -1) {
                sMaxAge = cache.sMaxAge
            }
            isMustRevalidate = cache.mustRevalidate
            isNoStore = cache.noStore
            isNoTransform = cache.noTransform
            isProxyRevalidate = cache.proxyRevalidate
        }
        configurable.register(CacheControlFilter(cacheControl))
        return true
    }

    private fun AnnotatedElement.registerNoCacheControl(configurable: FeatureContext): Boolean {
        val noCache = getAnnotation(NoCache::class.java) ?: return false
        val cacheControl = CacheControl().apply {
            isNoCache = true
            isNoTransform = false
            noCacheFields += noCache.fields
        }
        configurable.register(CacheControlFilter(cacheControl))
        return true
    }
}
