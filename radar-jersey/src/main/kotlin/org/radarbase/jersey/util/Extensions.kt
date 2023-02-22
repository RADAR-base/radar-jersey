package org.radarbase.jersey.util

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consume
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Transform each value in the iterable in a separate coroutine and await termination.
 */
internal suspend inline fun <T, R> Iterable<T>.forkJoin(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline transform: suspend CoroutineScope.(T) -> R
): List<R> = coroutineScope {
    map { t -> async(coroutineContext) { transform(t) } }
        .awaitAll()
}

/**
 * Consume the first value produced by the producer on its provided channel. Once a value is sent
 * by the producer, its coroutine is cancelled.
 * @throws kotlinx.coroutines.channels.ClosedReceiveChannelException if the producer does not
 *         produce any values.
 */
internal suspend inline fun <T> consumeFirst(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline producer: suspend CoroutineScope.(emit: suspend (T) -> Unit) -> Unit
): T = coroutineScope {
    val channel = Channel<T>()

    val producerJob = launch(coroutineContext) {
        producer(channel::send)
        channel.close()
    }

    val result = channel.consume { receive() }
    producerJob.cancel()
    result
}

suspend fun <T, R>  Iterable<T>.concurrentFirstOfNotNullOrNull(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    transform: suspend CoroutineScope.(T) -> R?
): R? = consumeFirst(coroutineContext) { emit ->
    forkJoin(coroutineContext) { t ->
        val result = transform(t)
        if (result != null) {
            emit(result)
        }
    }
    emit(null)
}

suspend fun <T> Iterable<T>.concurrentAny(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    predicate: suspend CoroutineScope.(T) -> Boolean
): Boolean = consumeFirst(coroutineContext) { emit ->
    forkJoin(coroutineContext) { t ->
        if (predicate(t)) {
            emit(true)
        }
    }
    emit(false)
}
