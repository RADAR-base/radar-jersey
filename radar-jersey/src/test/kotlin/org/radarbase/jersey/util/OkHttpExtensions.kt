package org.radarbase.jersey.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

inline fun <T> OkHttpClient.request(builder: Request.Builder.() -> Unit, callback: (Response) -> T): T =
    newCall(
        Request.Builder().apply(builder).build()
    ).execute().use(callback)

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
