package org.radarbase.jersey

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.net.URI

/**
 * Grizzly server wrapper.
 */
class GrizzlyServer(
        /** Base URI for the server to listen at. */
        private val baseUri: URI,
        /** ResourceConfig including all needed Jersey resources. */
        resources: ResourceConfig,
        /**
         * Whether to enable JMX. If true, ensure that additional JMX dependencies from Grizzly
         * are imported.
         */
        enableJmx: Boolean = false) {
    private val server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resources)
            .also { it.serverConfiguration.isJmxEnabled = enableJmx }

    private val shutdownHook = Thread(Runnable {
        logger.info("Stopping HTTP server...")
        server.shutdown()
    }, "shutdownHook")

    /** Start the server. This is a non-blocking call. */
    fun start() {
        server.start()
    }

    /**
     * Listen for connections indefinitely. This adds a shutdown hook to stop the server
     * once the JVM is shut down. Otherwise, this can be interrupted with an
     * InterruptedException. If an error occurs, {@link #shutdown()} should still be called.
     */
    fun listen() {
        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(shutdownHook)

        try {
            server.start()

            logger.info(String.format("Jersey app started on %s.\nPress Ctrl+C to exit...",
                    baseUri))
            Thread.currentThread().join()
        } catch (e: Exception) {
            logger.error("Error starting server: {}", e.toString())
        }
    }

    /**
     * Stop the HTTP server.
     */
    fun shutdown() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook)
        } catch (ex: IllegalStateException) {
            // ignore
        }
        server.shutdown()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GrizzlyServer::class.java)
    }
}