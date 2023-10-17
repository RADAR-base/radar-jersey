package org.radarbase.jersey.hibernate.mock.resource

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import kotlinx.coroutines.delay
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.db.ProjectRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import kotlin.time.Duration.Companion.seconds

@Path("projects")
@Consumes("application/json")
@Produces("application/json")
class ProjectResource(
    @Context private val projects: ProjectRepository,
    @Context private val asyncService: AsyncCoroutineService,
) {
    @POST
    @Path("query")
    fun query(@Suspended asyncResponse: AsyncResponse) = asyncService.runAsCoroutine(asyncResponse) {
        delay(1.seconds)
        "{\"result\": 1}"
    }

    @GET
    fun projects(@Suspended asyncResponse: AsyncResponse) = asyncService.runAsCoroutine(asyncResponse) {
        projects.list()
    }

    @GET
    @Path("empty")
    fun empty() = listOf<String>()

    @GET
    @Path("empty-suspend")
    fun emptySuspend(@Suspended asyncResponse: AsyncResponse) = asyncService.runAsCoroutine(asyncResponse) { listOf<String>() }

    @GET
    @Path("empty-blocking")
    fun emptyBlocking() = asyncService.runBlocking { listOf<String>() }

    @GET
    @Path("{id}")
    fun project(
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        projects.get(id)
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")
    }

    @POST
    @Path("{id}")
    fun updateProject(
        @PathParam("id") id: Long,
        values: Map<String, String>,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        projects.update(id, values["description"], values.getValue("organization"))
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")
    }

    @POST
    fun createProject(
        values: Map<String, String>,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        projects.create(values.getValue("name"), values["description"], values.getValue("organization"))
    }

    @DELETE
    @Path("{id}")
    fun deleteProject(
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) { projects.delete(id) }
}
