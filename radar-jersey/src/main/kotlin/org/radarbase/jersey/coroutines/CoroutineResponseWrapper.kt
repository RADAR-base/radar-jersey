package org.radarbase.jersey.coroutines

import kotlinx.coroutines.*
import org.glassfish.jersey.process.internal.RequestScope
import org.radarbase.jersey.exception.HttpServerUnavailableException
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CoroutineResponseWrapper(
    private val timeout: Duration? = 30.seconds,
    requestScope: RequestScope? = null,
    location: String? = null,
) {
    private val job = Job()

    val coroutineContext: CoroutineContext

    private val requestContext = try {
        requestScope?.createContext()
    } catch (ex: Throwable) {
        logger.debug("Cannot create request scope: {}", ex.toString())
        null
    }

    init {
        var context = job +
            CoroutineName("Request coroutine ${location ?: ""}#${Thread.currentThread().id}") +
            Dispatchers.Default

        if (requestContext != null) {
            context += CoroutineRequestContext(requestContext)
        }
        coroutineContext = context
    }

    fun cancel() {
        try {
            requestContext?.release()
        } catch (ex: Throwable) {
            // this is fine
        }
        job.cancel()
    }

    fun CoroutineScope.timeoutHandler(emit: suspend (Throwable) -> Unit) {
        timeout ?: return
        launch {
            delay(timeout)
            emit(HttpServerUnavailableException())
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoroutineResponseWrapper::class.java)
    }
}
