package org.radarbase.jersey.exception

import jakarta.ws.rs.core.Response

class HttpTimeoutException(
    message: String? = null,
    additionalHeaders: List<Pair<String, String>> = listOf(),
) : HttpApplicationException(
    status = Response.Status.REQUEST_TIMEOUT,
    code = "timeout",
    detailedMessage = message,
    additionalHeaders = additionalHeaders
)
