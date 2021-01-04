package org.radarbase.jersey.resource

import org.radarbase.jersey.service.HealthService
import javax.annotation.Resource
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType.APPLICATION_JSON

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
