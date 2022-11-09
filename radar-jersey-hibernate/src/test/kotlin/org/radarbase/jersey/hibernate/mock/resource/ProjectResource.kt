package org.radarbase.jersey.hibernate.mock.resource

import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.hibernate.db.ProjectDao
import org.radarbase.jersey.hibernate.db.ProjectRepository

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
    fun updateProject(@PathParam("id") id: Long, values: Map<String, String>) = projects.update(id, values["description"], values.getValue("organization"))
            ?: throw HttpNotFoundException("project_not_found", "Project with ID $id does not exist")

    @POST
    fun createProject(values: Map<String, String>) = projects.create(values.getValue("name"), values["description"], values.getValue("organization"))

    @DELETE
    @Path("{id}")
    fun deleteProject(@PathParam("id") id: Long) = projects.delete(id)
}
