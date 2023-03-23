package org.radarbase.jersey.hibernate.mock.resource

import jakarta.ws.rs.*
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import kotlinx.coroutines.delay
import org.radarbase.jersey.coroutines.runAsCoroutine
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.db.ProjectRepository
import kotlin.time.Duration.Companion.seconds

@Path("projects")
@Consumes("application/json")
@Produces("application/json")
class ProjectResource(
    @Context private val projects: ProjectRepository,
) {
    @POST
    @Path("query")
    fun query(@Suspended asyncResponse: AsyncResponse) = asyncResponse.runAsCoroutine {
        delay(1.seconds)
        "{\"result\": 1}"
    }

    @GET
    fun projects(@Suspended asyncResponse: AsyncResponse) = asyncResponse.runAsCoroutine {
        projects.list()
    }

    @GET
    @Path("{id}")
    fun project(
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncResponse.runAsCoroutine {
        projects.get(id)
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")
    }

    @POST
    @Path("{id}")
    fun updateProject(
        @PathParam("id") id: Long,
        values: Map<String, String>,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncResponse.runAsCoroutine {
        projects.update(id, values["description"], values.getValue("organization"))
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")
    }

    @POST
    fun createProject(
        values: Map<String, String>,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncResponse.runAsCoroutine {
        projects.create(values.getValue("name"), values["description"], values.getValue("organization"))
    }

    @DELETE
    @Path("{id}")
    fun deleteProject(
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncResponse.runAsCoroutine { projects.delete(id) }
}
