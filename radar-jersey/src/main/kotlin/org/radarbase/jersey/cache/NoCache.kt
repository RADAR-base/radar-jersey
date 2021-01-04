package org.radarbase.jersey.cache

/**
 * Set Cache-Control response header of "nocache"
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
    val fields: Array<String> = [],
)
