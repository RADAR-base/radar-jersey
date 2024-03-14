package org.radarbase.jersey.service

import jakarta.ws.rs.container.AsyncResponse
import kotlinx.coroutines.CancellableContinuation
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface AsyncCoroutineService {
    /**
     * Run a block with the async coroutine service contexts. If no request scope was created yet,
     * this will create one within the coroutine. It can be accessed via [runInRequestScope].
     */
    suspend fun <T> withContext(name: String = "AsyncCoroutineService.withContext", block: suspend () -> T): T

    /**
     * Run an AsyncResponse as a coroutine. The result of [block] will be used as the response. If
     * [block] throws any exception, that exception will be used to resume instead. If the connection
     * is cancelled by the client, the underlying job is also cancelled. If [timeout] is not null,
     * after the timeout has expired a 503 Server Unavailable exception will be thrown and the coroutine
     * will be cancelled. The current request scope is added to the coroutines scope to later be
     * used with [runInRequestScope] or [suspendInRequestScope].
     */
    fun <T> runAsCoroutine(
        asyncResponse: AsyncResponse,
        timeout: Duration = 30.seconds,
        block: suspend () -> T,
    )

    /**
     * Run a blocking request as a coroutine.  The result of [block] will be used as the response. If
     * [block] throws any exception, that exception will be used to resume instead. If the connection
     * is cancelled by the client, the underlying job is also cancelled. If [timeout] is not null,
     * after the timeout has expired a 503 Server Unavailable exception will be thrown and the coroutine
     * will be cancelled. The current request scope is added to the coroutines scope to later be
     * used with [runInRequestScope] or [suspendInRequestScope].
     */
    fun <T> runBlocking(
        timeout: Duration = 30.seconds,
        block: suspend () -> T,
    ): T

    /**
     * Run given function in request scope. No more coroutines or thread changes should be done
     * inside [block].
     */
    suspend fun <T> runInRequestScope(block: () -> T): T

    /**
     * Run given function in request scope. Usually the contents of the block will call a callback.
     * No more coroutines or thread changes should be done inside [block]. Allow cancelling the
     * block by configuring [CancellableContinuation.invokeOnCancellation]. Finish the function by
     * calling [CancellableContinuation.resumeWith] or any of its corresponding helper functions.
     */
    suspend fun <T> suspendInRequestScope(block: (CancellableContinuation<T>) -> Unit): T
}
