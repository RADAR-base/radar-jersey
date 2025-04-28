package org.radarbase.jersey.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.glassfish.jersey.process.internal.RequestContext
import org.glassfish.jersey.process.internal.RequestScope
import org.radarbase.jersey.exception.HttpServerUnavailableException
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CoroutineRequestWrapper(
    private val requestContext: RequestContext?,
    config: CoroutineRequestConfig,
) {
    private val job = Job()
    private val timeout = config.timeout

    val coroutineContext: CoroutineContext

    init {
        var context = job + contextName(config.location) + Dispatchers.Default
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
            emit(
                HttpServerUnavailableException(
                    "The co-routine that handles the request exceeded the max duration of " +
                        "$timeout. The request timeout can be configured as parameter to the runAsCoroutine() or runBlocking() function.",
                ),
            )
        }
    }

    companion object {

        @Suppress("DEPRECATION", "KotlinRedundantDiagnosticSuppress")
        private fun contextName(location: String?) = CoroutineName(
            "Request coroutine ${location ?: ""}#${Thread.currentThread().id}",
        )
    }
}

data class CoroutineRequestConfig(
    var timeout: Duration? = 30.seconds,
    var requestScope: RequestScope? = null,
    var location: String? = null,
)

fun CoroutineRequestWrapper(
    requestScope: RequestScope? = null,
    block: CoroutineRequestConfig.(hasExistingScope: Boolean) -> Unit,
): CoroutineRequestWrapper {
    var newlyCreated = false
    val requestContext = try {
        if (requestScope != null) {
            requestScope.suspendCurrent() ?: run {
                newlyCreated = true
                requestScope.createContext()
            }
        } else {
            null
        }
    } catch (ex: Throwable) {
        logger.debug("Cannot create request scope: {}", ex.toString())
        null
    }
    val config = CoroutineRequestConfig().apply {
        if (requestScope != null && requestContext != null) {
            requestScope.runInScope(requestContext) { block(!newlyCreated) }
        } else {
            block(!newlyCreated)
        }
    }
    return CoroutineRequestWrapper(requestContext, config)
}

private val logger = LoggerFactory.getLogger(CoroutineRequestWrapper::class.java)
