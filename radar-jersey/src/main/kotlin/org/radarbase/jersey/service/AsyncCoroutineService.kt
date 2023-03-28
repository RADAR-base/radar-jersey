package org.radarbase.jersey.service

import jakarta.ws.rs.container.AsyncResponse
import kotlinx.coroutines.CancellableContinuation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface AsyncCoroutineService {

    /**
     * Run an AsyncResponse as a coroutine. The result of [block] will be used as the response. If
     * [block] throws any exception, that exception will be used to resume instead. If the connection
     * is cancelled by the client, the underlying job is also cancelled. If [timeout] is not null,
     * after the timeout has expired a 503 Server Unavailable exception will be thrown and the coroutine
     * will be cancelled.
     */
    fun <T> runAsCoroutine(
        asyncResponse: AsyncResponse,
        timeout: Duration = 30.seconds,
        block: suspend () -> T,
    )

    fun <T> runBlocking(
        timeout: Duration = 30.seconds,
        block: suspend () -> T,
    ): T

    suspend fun <T> runInRequestScope(block: () -> T): T
    suspend fun <T> suspendInRequestScope(block: (CancellableContinuation<T>) -> Unit): T
}
