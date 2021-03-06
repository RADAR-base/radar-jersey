package org.radarbase.jersey.resource

import org.radarbase.jersey.service.HealthService
import jakarta.annotation.Resource
import jakarta.inject.Singleton
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON

@Path("/health")
@Resource
@Singleton
class HealthResource(
        @Context private val healthService: HealthService
) {
    @GET
    @Produces(APPLICATION_JSON)
    fun healthStatus(): Map<String, Any> = healthService.metrics
}
