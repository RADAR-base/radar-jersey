package org.radarbase.jersey.hibernate.mock.resource

import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.db.ProjectDao
import org.radarbase.jersey.hibernate.db.ProjectRepository
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context

@Path("projects")
@Consumes("application/json")
@Produces("application/json")
class ProjectResource(
        @Context private val projects: ProjectRepository
) {
    @GET
    fun projects(): List<ProjectDao> = projects.list()

    @GET
    @Path("{id}")
    fun project(@PathParam("id") id: Long) = projects.get(id)
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")

    @POST
    @Path("{id}")
    fun updateProject(@PathParam("id") id: Long, values: Map<String, String>) = projects.update(id, values["description"])
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")

    @POST
    fun createProject(values: Map<String, String>) = projects.create(values.getValue("name"), values["description"])

    @DELETE
    @Path("{id}")
    fun deleteProject(@PathParam("id") id: Long) = projects.delete(id)
}
