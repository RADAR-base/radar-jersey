/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.filter

import java.io.IOException
import javax.inject.Singleton
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter
import javax.ws.rs.ext.Provider

@Provider
@Singleton
class CorsFilter : ContainerResponseFilter {
    @Throws(IOException::class)
    override fun filter(request: ContainerRequestContext,
               response: ContainerResponseContext) {
        response.headers.add("Access-Control-Allow-Origin",
                request.getHeaderString("Origin") ?: "*")
        response.headers.add("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization")
        response.headers.add("Access-Control-Allow-Credentials", "true")
        response.headers.add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD")
    }
}
