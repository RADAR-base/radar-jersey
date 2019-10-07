package org.radarbase.jersey

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.net.URI

class GrizzlyServer(private val baseUri: URI, resources: ResourceConfig, enableJmx: Boolean = false) {
    private val server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resources)
            .also { it.serverConfiguration.isJmxEnabled = enableJmx }

    private val shutdownHook = Thread(Runnable {
        logger.info("Stopping server..")
        server.shutdown()
    }, "shutdownHook")

    fun start() {
        server.start()
    }

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
