package org.radarbase.jersey.service

import jakarta.inject.Provider
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.ConnectionCallback
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.UriInfo
import kotlinx.coroutines.*
import org.glassfish.jersey.process.internal.RequestScope
import org.radarbase.jersey.coroutines.CoroutineRequestContext
import org.radarbase.jersey.coroutines.CoroutineRequestWrapper
import org.radarbase.kotlin.coroutines.consumeFirst
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
        with(
            CoroutineRequestWrapper(
                timeout,
                requestScope.get(),
                "${requestContext.get().method} ${uriInfo.get().path}",
            ),
        ) {
            asyncResponse.register(ConnectionCallback { cancel() })

            CoroutineScope(coroutineContext).launch {
                timeoutHandler {
                    asyncResponse.resume(it)
                    this@with.cancel()
                }
                try {
                    asyncResponse.resume(block())
                } catch (ex: CancellationException) {
                    // do nothing
                } catch (ex: Throwable) {
                    asyncResponse.resume(ex)
                } finally {
                    this@with.cancel()
                }
            }
        }
    }

    override fun <T> runBlocking(
        timeout: Duration,
        block: suspend () -> T,
    ): T {
        return with(
            CoroutineRequestWrapper(
                timeout,
                requestScope.get(),
                "${requestContext.get().method} ${uriInfo.get().path}",
            ),
        ) {
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
                this@with.cancel()
            }
        }
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
}
