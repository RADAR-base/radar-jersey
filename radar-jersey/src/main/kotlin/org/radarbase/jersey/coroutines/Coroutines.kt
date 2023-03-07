package org.radarbase.jersey.coroutines

import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.ConnectionCallback
import kotlinx.coroutines.*
import org.radarbase.jersey.exception.HttpServerUnavailableException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Run an AsyncResponse as a coroutine. The result of [block] will be used as the response. If
 * [block] throws any exception, that exception will be used to resume instead. If the connection
 * is cancelled by the client, the underlying job is also cancelled. If [timeout] is not null,
 * after the timeout has expired a 503 Server Unavailable exception will be thrown and the coroutine
 * will be cancelled.
 */
fun <T> AsyncResponse.runAsCoroutine(
    timeout: Duration? = 30.seconds,
    block: suspend () -> T,
) {
    val job = Job()

    val emit: (T) -> Unit = { value ->
        resume(value)
        job.cancel()
    }
    val cancel: (Throwable) -> Unit = { ex ->
        resume(ex)
        job.cancel()
    }

    register(ConnectionCallback { job.cancel() })

    CoroutineScope(job + Dispatchers.Default).launch {
        if (timeout != null) {
            launch {
                delay(timeout)
                cancel(HttpServerUnavailableException())
            }
        }
        try {
            emit(block())
        } catch (ex: CancellationException) {
            // do nothing
            job.cancel(ex)
        } catch (ex: Throwable) {
            cancel(ex)
        }
    }
}
