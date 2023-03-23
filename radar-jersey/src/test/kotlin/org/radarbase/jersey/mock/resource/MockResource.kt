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
import jakarta.annotation.Resource
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.exception.HttpBadRequestException
import java.io.IOException
import java.time.Instant

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
    fun someUser(@Context radarToken: RadarToken): Map<String, String> {
        return mapOf("accessToken" to (radarToken.token ?: ""))
    }

    @Authenticated
    @GET
    @Path("user/detailed")
    fun someUserDetailed(@Context radarToken: RadarToken): DetailedUser {
        return DetailedUser((radarToken.token ?: ""), "name")
    }

    @Authenticated
    @GET
    @Path("projects/{projectId}/users/{subjectId}")
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    @Operation(description = "Get user that is subject in given project")
    @ApiResponses(
        value = [
            ApiResponse(description = "User"),
        ],
    )
    fun mySubject(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") userId: String,
    ): Map<String, String> {
        return mapOf("projectId" to projectId, "userId" to userId)
    }

    @GET
    @Path("exception")
    fun withException(): Unit = throw IOException("Test")

    @GET
    @Path("badrequest")
    fun withBadRequestException(): Unit = throw HttpBadRequestException("code", "message")

    @GET
    @Path("jerseybadrequest")
    fun withJerseyBadRequestException(): Unit = throw BadRequestException("test")

    @POST
    @Path("user")
    fun updateUser(user: DetailedUser): DetailedUser {
        return user
    }

    data class DetailedUser(
        val accessToken: String,
        val name: String,
        val createdAt: Instant = Instant.ofEpochSecond(3600),
    )
}
