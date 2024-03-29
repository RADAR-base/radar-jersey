package org.radarbase.jersey.enhancer

import io.swagger.v3.oas.models.OpenAPI
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.disabled.DisabledAuthorizationResourceEnhancer
import org.radarbase.jersey.auth.jwt.EcdsaResourceEnhancer
import org.radarbase.jersey.auth.managementportal.ManagementPortalResourceEnhancer
import org.radarbase.jersey.doc.swagger.SwaggerResourceEnhancer
import org.radarbase.jersey.exception.ExceptionResourceEnhancer

object Enhancers {
    /** Adds authorization framework, configuration and utilities. */
    fun radar(
        config: AuthConfig,
        includeMapper: Boolean = true,
    ) = RadarJerseyResourceEnhancer(config, includeMapper = includeMapper)

    /** Authorization via ManagementPortal. */
    fun managementPortal(config: AuthConfig) = ManagementPortalResourceEnhancer(config)

    /** Disable all authorization. Useful for a public service. */
    val disabledAuthorization = DisabledAuthorizationResourceEnhancer()

    /** Handle a generic ECDSA identity provider. */
    val ecdsa = EcdsaResourceEnhancer()

    /** Adds a health endpoint. */
    val health = HealthResourceEnhancer()

    /**
     * Handles any application exceptions including an appropriate response to client.
     * @see org.radarbase.jersey.exception.HttpApplicationException
     */
    val exception = ExceptionResourceEnhancer()

    /** Add ObjectMapper utility. Not needed if radar(includeMapper = true). */
    val mapper = MapperResourceEnhancer()

    /**
     * Adds an OpenAPI endpoint to the stack at `/openapi.yaml` and `/openapi.json`.
     * The description is given with [openApi]. Any routes provided in
     * [ignoredRoutes] will not be shown in the endpoint.
     */
    fun swagger(openApi: OpenAPI, ignoredRoutes: Set<String>? = null) = SwaggerResourceEnhancer(openApi, ignoredRoutes)
}
