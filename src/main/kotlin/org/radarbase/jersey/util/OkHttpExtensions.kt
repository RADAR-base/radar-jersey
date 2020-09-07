package org.radarbase.jersey.util

import com.fasterxml.jackson.databind.ObjectReader
import okhttp3.OkHttpClient
import okhttp3.Request
import org.radarbase.jersey.exception.HttpBadGatewayException
import org.slf4j.LoggerFactory

fun <T> OkHttpClient.requestJson(request: Request, reader: ObjectReader): T {
    return newCall(request).execute().use { response ->
        if (response.isSuccessful) {
            response.body?.byteStream()
                    ?.let { reader.readValue<T>(it) }
                    ?: throw HttpBadGatewayException("ManagementPortal did not provide a result")
        } else {
            logger.error("Cannot connect to {}: HTTP status {} - {}", request.url, response.code, response.body?.string())
            throw HttpBadGatewayException("Cannot connect to ${request.url}: HTTP status ${response.code}")
        }
    }
}

fun OkHttpClient.request(request: Request): Boolean {
    return newCall(request).execute().use { response ->
        response.isSuccessful
    }
}

private val logger = LoggerFactory.getLogger(OkHttpClient::class.java)
