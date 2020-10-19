package org.radarbase.jersey.util

import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock

inline fun <T> Lock.locked(method: () -> T): T {
    lock()
    return try {
        method()
    } finally {
        unlock()
    }
}

inline fun <T> Semaphore.tryAcquired(nanos: Long? = null, method: () -> T): T? {
    val isAcquired = nanos?.let { tryAcquire(it, TimeUnit.NANOSECONDS) } ?: tryAcquire()
    return if (isAcquired) {
        try {
            method()
        } finally {
            release()
        }
    } else null
}

inline fun <T> Semaphore.acquired(method: () -> T): T {
    acquire()
    return try {
        method()
    } finally {
        release()
    }
}
