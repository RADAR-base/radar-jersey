package org.radarbase.jersey.util

import com.fasterxml.jackson.databind.ObjectReader
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.radarbase.jersey.exception.HttpBadGatewayException
import org.slf4j.LoggerFactory

inline fun <T> OkHttpClient.request(builder: Request.Builder.() -> Unit, callback: (Response) -> T): T =
    newCall(
        Request.Builder().apply(builder).build()
    ).execute().use(callback)

/**
 * Make a [request] and resolves it as JSON using given object reader. [T] is the type that the
 * [reader] is initialized with using [com.fasterxml.jackson.databind.ObjectMapper.readerFor].
 * @throws ClassCastException if the [reader] is not initialized for the correct class.
 * @throws java.io.IOException if the request failed or the JSON cannot be parsed.
 * @throws HttpBadGatewayException if the response had an unsuccessful HTTP status code.
 */
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

/**
 * Make a [request] and checks the HTTP status code of the response.
 * @return true if the call returned a successful HTTP status code, false otherwise.
 * @throws java.io.IOException if the request failed
 */
fun OkHttpClient.request(request: Request): Boolean {
    return newCall(request).execute().use { response ->
        response.isSuccessful
    }
}

private val logger = LoggerFactory.getLogger(OkHttpClient::class.java)
