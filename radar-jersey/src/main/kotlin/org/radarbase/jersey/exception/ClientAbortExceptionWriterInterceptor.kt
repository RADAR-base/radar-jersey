package org.radarbase.jersey.exception

import jakarta.annotation.Priority
import jakarta.ws.rs.ext.Provider
import jakarta.ws.rs.ext.WriterInterceptor
import jakarta.ws.rs.ext.WriterInterceptorContext
import org.glassfish.jersey.server.internal.process.MappableException
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Ignore exceptions when writing a response, which almost always means the
 * client disconnected before reading the full response.
 */
@Provider
@Priority(1)
class ClientAbortExceptionWriterInterceptor : WriterInterceptor {
    @Throws(IOException::class)
    override fun aroundWriteTo(context: WriterInterceptorContext) {
        try {
            context.proceed()
        } catch (ex: MappableException) {
            var cause: Throwable? = ex
            while (cause != null) {
                if (cause is IOException && cause.message == "Connection is closed") {
                    logger.warn("Client aborted request.")
                    return
                }
                cause = cause.cause
            }
            if (ex.cause != null) {
                logger.error("Failed to write response: {}", ex.message, ex.cause)
            } else {
                logger.error("Failed to write response", ex)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ClientAbortExceptionWriterInterceptor::class.java)
    }
}
