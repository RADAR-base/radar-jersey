/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.mock.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Auth
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import jakarta.annotation.Resource
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType

@Path("/")
@Resource
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class MockResource {
    @GET
    fun something(): Map<String, String> {
        return mapOf("this" to "that")
    }

    @Authenticated
    @GET
    @Path("user")
    fun someUser(@Context auth: Auth): Map<String, String> {
        return mapOf("accessToken" to auth.token.token)
    }

    @Authenticated
    @GET
    @Path("user/detailed")
    fun someUserDetailed(@Context auth: Auth): DetailedUser {
        return DetailedUser(auth.token.token, "name")
    }

    @Authenticated
    @GET
    @Path("projects/{projectId}/users/{subjectId}")
    @NeedsPermission(Permission.Entity.SUBJECT, Permission.Operation.READ, "projectId", "subjectId")
    @Operation(description = "Get user that is subject in given project")
    @ApiResponses(value = [
        ApiResponse(description = "User")
    ])
    fun mySubject(
            @PathParam("projectId") projectId: String,
            @PathParam("subjectId") userId: String): Map<String, String> {
        return mapOf("projectId" to projectId, "userId" to userId)
    }

    data class DetailedUser(val accessToken: String, val name: String)
}
