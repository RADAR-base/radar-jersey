package org.radarbase.jersey.service

import jakarta.inject.Provider
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.ConnectionCallback
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.UriInfo
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.glassfish.jersey.process.internal.RequestScope
import org.radarbase.jersey.coroutines.CoroutineRequestContext
import org.radarbase.jersey.coroutines.CoroutineRequestWrapper
import org.radarbase.kotlin.coroutines.consumeFirst
import org.slf4j.LoggerFactory
import kotlin.time.Duration

class ScopedAsyncCoroutineService(
    @Context private val requestScope: Provider<RequestScope>,
    @Context private val requestContext: Provider<ContainerRequestContext>,
    @Context private val uriInfo: Provider<UriInfo>,
) : AsyncCoroutineService {

    override fun <T> runAsCoroutine(
        asyncResponse: AsyncResponse,
        timeout: Duration,
        block: suspend () -> T,
    ) {
        withWrapper(timeout) {
            asyncResponse.register(ConnectionCallback { cancelRequest() })

            CoroutineScope(coroutineContext).launch {
                timeoutHandler {
                    asyncResponse.resume(it)
                    cancelRequest()
                }
                try {
                    asyncResponse.resume(block())
                } catch (ex: CancellationException) {
                    // do nothing, cancel request in finally
                } catch (ex: Throwable) {
                    asyncResponse.resume(ex)
                } finally {
                    cancelRequest()
                }
            }
        }
    }

    override suspend fun <T> withContext(name: String, block: suspend () -> T): T {
        // continue in existing context
        return if (currentCoroutineContext()[CoroutineRequestContext.Key] != null) {
            block()
        } else {
            val scope = try {
                requestScope.get()
            } catch (ex: Exception) {
                logger.debug("Cannot construct request scope inside withContext", ex)
                null
            }
            val wrapper = CoroutineRequestWrapper(scope) {
                timeout = null
                location = name
            }
            try {
                withContext(wrapper.coroutineContext) {
                    block()
                }
            } finally {
                wrapper.cancelRequest()
            }
        }
    }

    override fun <T> runBlocking(
        timeout: Duration,
        block: suspend () -> T,
    ): T = withWrapper(timeout) {
        try {
            runBlocking(coroutineContext) {
                consumeFirst<Result<T>> { emit ->
                    timeoutHandler { emit(Result.failure(it)) }
                    try {
                        val result = block()
                        emit(Result.success(result))
                    } catch (ex: CancellationException) {
                        throw ex
                    } catch (ex: Throwable) {
                        emit(Result.failure(ex))
                    }
                }.getOrThrow()
            }
        } finally {
            cancelRequest()
        }
    }

    private inline fun <V> withWrapper(timeout: Duration? = null, block: CoroutineRequestWrapper.() -> V): V {
        val scope = requestScope.get()
        val wrapper = CoroutineRequestWrapper(scope) { hasExistingScope ->
            this.timeout = timeout
            location = if (hasExistingScope) {
                "${requestContext.get().method} ${uriInfo.get().path}"
            } else {
                "asyncCoroutine"
            }
        }
        return wrapper.block()
    }

    override suspend fun <T> runInRequestScope(block: () -> T): T {
        val requestContext = checkNotNull(currentCoroutineContext()[CoroutineRequestContext.Key]) {
            """Not running inside a request scope. Initialize with AsyncCoroutineService.runAsCoroutine or supply a RequestScope to AsyncResponse.runAsCoroutine."""
        }
        return requestScope.get().runInScope(
            requestContext.requestContext,
            block,
        )
    }

    override suspend fun <T> suspendInRequestScope(block: (CancellableContinuation<T>) -> Unit): T {
        val requestContext = checkNotNull(currentCoroutineContext()[CoroutineRequestContext.Key]) {
            """Not running inside a request scope. Initialize with AsyncCoroutineService.runAsCoroutine or supply a RequestScope to AsyncResponse.runAsCoroutine."""
        }
        return suspendCancellableCoroutine { continuation ->
            requestScope.get().runInScope(requestContext.requestContext) {
                block(continuation)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ScopedAsyncCoroutineService::class.java)
    }
}
