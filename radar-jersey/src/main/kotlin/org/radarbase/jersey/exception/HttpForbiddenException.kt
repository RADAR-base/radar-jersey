/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception

import jakarta.ws.rs.core.Response

class HttpForbiddenException(
    code: String,
    detailedMessage: String,
    wwwAuthenticateHeader: String? = null,
    additionalHeaders: List<Pair<String, String>> = listOf(),
) : HttpApplicationException(
    Response.Status.FORBIDDEN,
    code,
    detailedMessage,
    additionalHeaders = buildList(additionalHeaders.size + 1) {
        if (wwwAuthenticateHeader != null) {
            add("WWW-Authenticate" to wwwAuthenticateHeader)
        }
        addAll(additionalHeaders)
    },
)
