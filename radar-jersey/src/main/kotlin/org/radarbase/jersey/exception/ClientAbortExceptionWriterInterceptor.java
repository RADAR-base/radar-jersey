package org.radarbase.jersey.exception;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Ignore exceptions when writing a response, which almost always means the
 * client disconnected before reading the full response.
 */
@Provider
@Priority(1)
public class ClientAbortExceptionWriterInterceptor implements WriterInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ClientAbortExceptionWriterInterceptor.class);

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
        try {
            context.proceed();
        } catch (MappableException ex) {
            for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                if (cause instanceof IOException && cause.getMessage().equals("Connection is closed")) {
                    logger.warn("Client aborted request.", cause);
                    return;
                }
            }
            if (ex.getCause() != null) {
                logger.error("Failed to write response: {}", ex.getMessage(), ex.getCause());
            } else {
                logger.error("Failed to write response", ex);
            }
        }
    }
}
