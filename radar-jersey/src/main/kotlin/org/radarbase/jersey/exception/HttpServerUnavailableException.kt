package org.radarbase.jersey.exception

import jakarta.ws.rs.core.Response

class HttpServerUnavailableException(
    message: String? = null,
    additionalHeaders: List<Pair<String, String>> = listOf(),
) : HttpApplicationException(
    status = Response.Status.SERVICE_UNAVAILABLE,
    code = "timeout",
    detailedMessage = message,
    additionalHeaders = additionalHeaders
)
