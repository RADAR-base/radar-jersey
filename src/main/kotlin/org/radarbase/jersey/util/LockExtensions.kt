package org.radarbase.jersey.util

import java.util.concurrent.locks.Lock

inline fun <T> Lock.locked(method: () -> T): T {
    lock()
    return try {
        method()
    } finally {
        unlock()
    }
}

inline fun <T> Lock.tryLocked(method: () -> T): T? {
    return if (tryLock()) {
        try {
            method()
        } finally {
            unlock()
        }
    } else null
}
