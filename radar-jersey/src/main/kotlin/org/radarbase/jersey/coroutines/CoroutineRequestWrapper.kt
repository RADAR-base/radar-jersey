package org.radarbase.jersey.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.glassfish.jersey.process.internal.RequestScope
import org.radarbase.jersey.exception.HttpServerUnavailableException
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CoroutineRequestWrapper(
    private val timeout: Duration? = 30.seconds,
    requestScope: RequestScope? = null,
    location: String? = null,
) {
    private val job = Job()

    val coroutineContext: CoroutineContext

    private val requestContext = try {
        if (requestScope != null) {
            requestScope.suspendCurrent()
                ?: requestScope.createContext()
        } else {
            null
        }
    } catch (ex: Throwable) {
        logger.debug("Cannot create request scope: {}", ex.toString())
        null
    }

    init {
        var context = job + contextName(location) + Dispatchers.Default
        if (requestContext != null) {
            context += CoroutineRequestContext(requestContext)
        }
        coroutineContext = context
    }

    fun cancelRequest() {
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
        private val logger = LoggerFactory.getLogger(CoroutineRequestWrapper::class.java)

        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
        private fun contextName(location: String?) = CoroutineName(
            "Request coroutine ${location ?: ""}#${Thread.currentThread().id}",
        )
    }
}
