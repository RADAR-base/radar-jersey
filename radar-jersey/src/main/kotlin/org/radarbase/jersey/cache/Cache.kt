package org.radarbase.jersey.cache

/**
 * Set response Cache-Control header automatically. For the interpretation of the headers.
 *
 *  **See Also** [Cache-Control - HTTP &pipe; MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control)
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
@Retention(AnnotationRetention.RUNTIME)
annotation class Cache(
    /** Corresponds to the `max-age` cache control directive. */
    val maxAge: Int = -1,
    /** Corresponds to the `s-maxage` cache control directive. */
    val sMaxAge: Int = -1,
    /** Corresponds to the `no-store` cache control directive. */
    val noStore: Boolean = false,
    /** Corresponds to the `no-transform` cache control directive. */
    val noTransform: Boolean = false,
    /** Corresponds to the `must-revalidate` cache control directive. */
    val mustRevalidate: Boolean = false,
    /** Corresponds to the `proxy-revalidate` cache control directive. */
    val proxyRevalidate: Boolean = false,
    /** Corresponds to the `private` cache control directive. */
    val isPrivate: Boolean = false,
)
