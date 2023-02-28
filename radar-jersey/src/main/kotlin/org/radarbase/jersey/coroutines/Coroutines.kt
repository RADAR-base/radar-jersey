package org.radarbase.jersey.coroutines

import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.ConnectionCallback
import kotlinx.coroutines.*
import org.radarbase.jersey.exception.HttpTimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun AsyncResponse.runAsCoroutine(
    timeout: Duration? = 30.seconds,
    block: suspend () -> Any,
) {
    val job = Job()

    val emit: (Any) -> Unit = { value ->
        resume(value)
        job.cancel()
    }

    register(ConnectionCallback { job.cancel() })

    CoroutineScope(job).launch {
        if (timeout != null) {
            launch {
                delay(timeout)
                emit(HttpTimeoutException())
            }
        }
        try {
            emit(block())
        } catch (ex: CancellationException) {
            // do nothing
        } catch (ex: Throwable) {
            emit(ex)
        }
    }
}
