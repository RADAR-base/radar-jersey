package org.radarbase.jersey.filter.cache

/**
 * Set Cache-Control response header of `no-cache`.
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
annotation class NoCache(
    /** Additional Cache-Control fields to include along with the `no-cache` field. */
    val fields: Array<String> = [],
)
