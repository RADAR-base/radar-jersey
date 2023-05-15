package org.radarbase.jersey.resource

import jakarta.annotation.Resource
import jakarta.inject.Singleton
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.HealthService
import kotlin.time.Duration.Companion.seconds

@Path("/health")
@Resource
@Singleton
class HealthResource(
    @Context private val asyncService: AsyncCoroutineService,
    @Context private val healthService: HealthService,
) {
    @GET
    @Produces(APPLICATION_JSON)
    fun healthStatus(@Suspended asyncResponse: AsyncResponse) = asyncService.runAsCoroutine(asyncResponse, 5.seconds) {
        healthService.computeMetrics()
    }
}
