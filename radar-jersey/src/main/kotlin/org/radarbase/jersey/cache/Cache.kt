package org.radarbase.jersey.cache

/**
 * Set response Cache-Control header automatically.
 *
 * @author [Bill Burke](mailto:bill@burkecentral.com)
 * @version $Revision: 1 $
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Cache(
    val maxAge: Int = -1,
    val sMaxAge: Int = -1,
    val noStore: Boolean = false,
    val noTransform: Boolean = false,
    val mustRevalidate: Boolean = false,
    val proxyRevalidate: Boolean = false,
    val isPrivate: Boolean = false,
)
