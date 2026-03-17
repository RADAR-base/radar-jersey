package org.radarbase.jersey.doc.swagger

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.models.OpenAPI
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

/**
 * Adds an OpenAPI endpoint to the stack at `/openapi.yaml` and `/openapi.json`,
 * and an interactive Swagger UI at `/swagger`.
 * The description is given with [openApi]. Any routes provided in
 * [ignoredRoutes] will not be shown in the endpoint.
 */
class SwaggerResourceEnhancer(
    private val openApi: OpenAPI,
    private val ignoredRoutes: Set<String>? = null,
) : JerseyResourceEnhancer {

    override val classes: Array<Class<*>> = arrayOf(
        io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource::class.java,
        io.swagger.v3.jaxrs2.integration.resources.OpenApiResource::class.java,
        SwaggerUiResource::class.java,
    )

    override fun AbstractBinder.enhance() {
        bind(openApi).to(OpenAPI::class.java)
    }

    override fun ResourceConfig.enhance() {
        val oasConfig = SwaggerConfiguration().apply {
            openAPI = this@SwaggerResourceEnhancer.openApi
            this@SwaggerResourceEnhancer.ignoredRoutes?.let {
                this.ignoredRoutes = setOf("/application.wadl", "/swagger") + it
            } ?: run {
                this.ignoredRoutes = setOf("/application.wadl", "/swagger")
            }
            prettyPrint(true)
            cacheTTL = 3600
        }

        JaxrsOpenApiContextBuilder<JaxrsOpenApiContextBuilder<*>>()
            .application(this)
            .openApiConfiguration(oasConfig)
            .buildContext(true)
    }
}
