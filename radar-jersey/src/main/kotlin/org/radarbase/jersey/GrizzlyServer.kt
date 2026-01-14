/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey

import org.glassfish.grizzly.threadpool.ThreadPoolConfig
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.concurrent.TimeUnit

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
    enableJmx: Boolean = false,
    /**
     * Optional number of worker threads in the main Grizzly thread pool.
     * If null, Grizzly's default configuration is used.
     */
    workerCorePoolSize: Int? = null,
    /**
     * Optional maximum number of worker threads in the main Grizzly thread pool.
     * If null, Grizzly's default configuration is used.
     */
    workerMaxPoolSize: Int? = null,
) {
    private val server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resources).apply {
        serverConfiguration.isJmxEnabled = enableJmx

        if (workerCorePoolSize != null || workerMaxPoolSize != null) {
            listeners.forEach { listener ->
                listener.transport.workerThreadPoolConfig = ThreadPoolConfig.defaultConfig().apply {
                    workerCorePoolSize?.let(::setCorePoolSize)
                    workerMaxPoolSize?.let(::setMaxPoolSize)
                }
            }
        }
    }

    private val shutdownHook = Thread(
        {
            logger.info("Stopping HTTP server...")
            server.shutdown()
        },
        "shutdownHook",
    )

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

            logger.info(
                String.format(
                    "Jersey app started on %s.\nPress Ctrl+C to exit...",
                    baseUri,
                ),
            )
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
        server.shutdown(15, TimeUnit.SECONDS)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GrizzlyServer::class.java)
    }
}
