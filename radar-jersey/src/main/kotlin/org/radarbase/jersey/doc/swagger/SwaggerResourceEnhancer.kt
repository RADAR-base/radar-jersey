package org.radarbase.jersey.doc.swagger

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder
import io.swagger.v3.oas.integration.SwaggerConfiguration
import io.swagger.v3.oas.models.OpenAPI
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.config.JerseyResourceEnhancer

class SwaggerResourceEnhancer(
    private val openApi: OpenAPI,
    private val ignoredRoutes: Set<String>? = null,
) : JerseyResourceEnhancer {

    override val classes: Array<Class<*>> = arrayOf(
        io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource::class.java,
        io.swagger.v3.jaxrs2.integration.resources.OpenApiResource::class.java,
    )

    override fun ResourceConfig.enhance() {
        val oasConfig = SwaggerConfiguration().apply {
            openAPI = this@SwaggerResourceEnhancer.openApi
            this@SwaggerResourceEnhancer.ignoredRoutes?.let {
                this.ignoredRoutes = setOf("/application.wadl") + it
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
