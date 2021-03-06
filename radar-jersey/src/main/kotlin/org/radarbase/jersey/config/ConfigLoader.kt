package org.radarbase.jersey.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.models.OpenAPI
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.cache.CacheControlFeature
import org.radarbase.jersey.doc.swagger.SwaggerResourceEnhancer
import org.radarbase.jersey.filter.CorsFilter
import org.radarbase.jersey.filter.ResponseLoggerFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object ConfigLoader {
    /**
     * Load resources using a EnhancerFactory.
     *
     * @param factoryClass: EnhancerFactory implementation class.
     * @param parameters: parameters that should be passed to the EnhancerFactory constructor.
     *      Ensure that all factories in your project have a same constructor signature for this to
     *      work.
     * @throws NoSuchMethodException if the constructor cannot be found
     * @throws ReflectiveOperationException if the class cannot be instantiated
     */
    fun loadResources(factoryClass: Class<out EnhancerFactory>, vararg parameters: Any): ResourceConfig {
        val parameterClasses = parameters.map { it.javaClass }.toTypedArray()
        val enhancerFactory = factoryClass.getConstructor(*parameterClasses)
                .newInstance(*parameters)
        return createResourceConfig(enhancerFactory.createEnhancers())
    }

    @JvmOverloads
    fun <T> loadConfig(fileName: String, args: Array<String>, clazz: Class<T>, mapper: ObjectMapper? = null): T =
            loadConfig(listOf(fileName), args, clazz, mapper)

    @JvmOverloads
    fun <T> loadConfig(fileNames: List<String>, args: Array<String>, clazz: Class<T>, mapper: ObjectMapper? = null): T {
        val configFile = if (args.size == 1) Paths.get(args[0])
                else fileNames.map { Paths.get(it) }.find { Files.exists(it) }
        requireNotNull(configFile) { "Configuration not provided." }

        logger.info("Reading configuration from {}", configFile.toAbsolutePath())
        try {
            val localMapper = mapper ?: ObjectMapper(YAMLFactory())
                    .registerModule(KotlinModule())
            return Files.newInputStream(configFile).use { input ->
                BufferedInputStream(input).use { bufInput ->
                    localMapper.readValue(bufInput, clazz)
                }
            }
        } catch (ex: IOException) {
            logger.error("Usage: <command> [$configFile]")
            logger.error("Failed to read config file $configFile: ${ex.message}")
            exitProcess(1)
        }
    }

    /**
     * Load a configuration from YAML file. The filename is searched in the current working
     * directory. This exits with a usage information message if the file cannot be loaded.
     *
     * @throws IllegalArgumentException if a file matching configFileName cannot be found
     */
    inline fun <reified T> loadConfig(fileName: String, args: Array<String>, mapper: ObjectMapper? = null): T =
        loadConfig(listOf(fileName), args, T::class.java, mapper)

    /**
     * Load a configuration from YAML file. The filename is searched in the current working
     * directory. This exits with a usage information message if the file cannot be loaded.
     *
     * @throws IllegalArgumentException if a file matching configFileName cannot be found
     */
    inline fun <reified T> loadConfig(fileNames: List<String>, args: Array<String>, mapper: ObjectMapper? = null): T =
            loadConfig(fileNames, args, T::class.java, mapper)

    /**
     * Create a resourceConfig based on the provided resource enhancers. This method also disables
     * the WADL since it may be identified as a security risk.
     */
    fun createResourceConfig(enhancers: List<JerseyResourceEnhancer>): ResourceConfig {
        val resources = ResourceConfig()
        resources.property("jersey.config.server.wadl.disableWadl", true)
        enhancers.forEach { enhancer ->
            resources.packages(*enhancer.packages)
            resources.registerClasses(*enhancer.classes)
            enhancer.enhanceResources(resources)
        }

        resources.register(object : AbstractBinder() {
            override fun configure() {
                enhancers.forEach { it.enhanceBinder(this) }
            }
        })
        return resources
    }

    val logger: Logger = LoggerFactory.getLogger(ConfigLoader::class.java)

    object Filters {
        /** Adds CORS headers to all responses. */
        val cors = CorsFilter::class.java
        /** Log the HTTP status responses of all requests. */
        val logResponse = ResponseLoggerFilter::class.java
        /** Add cache control headers to responses. */
        val cache = CacheControlFeature::class.java
    }
    object Enhancers {
        /** Adds authorization framework, configuration and utilities. */
        fun radar(config: AuthConfig) = RadarJerseyResourceEnhancer(config)
        /** Authorization via ManagementPortal. */
        fun managementPortal(config: AuthConfig) = ManagementPortalResourceEnhancer(config)
        /** Disable all authorization. Useful for a public service. */
        val disabledAuthorization = DisabledAuthorizationResourceEnhancer()
        /** Handle a generic ECDSA identity provider. */
        val ecdsa = EcdsaResourceEnhancer()
        /** Adds a health endpoint. */
        val health = HealthResourceEnhancer()
        /**
         * Handles any HTTP application exceptions including an appropriate response to client.
         * @see org.radarbase.jersey.exception.HttpApplicationException
         */
        val httpException = HttpExceptionResourceEnhancer()
        /** Handle unhandled exceptions. */
        val generalException = GeneralExceptionResourceEnhancer()
        /** Adds OkHttp and ObjectMapper utilities. */
        val utility = UtilityResourceEnhancer()
        /**
         * Adds an OpenAPI endpoint to the stack at `/openapi.yaml` and `/openapi.json`.
         * The description is given with [openApi]. Any routes provided in
         * [ignoredRoutes] will not be shown in the endpoint.
         */
        fun swagger(openApi: OpenAPI, ignoredRoutes: Set<String>? = null) = SwaggerResourceEnhancer(openApi, ignoredRoutes)
    }

    inline fun <T> T.copyEnv(key: String, doCopy: T.(String?) -> T): T = copyOnChange<T, String?>(null, { System.getenv(key) }, doCopy)

    inline fun <T, V> T.copyOnChange(original: V, modification: (V) -> V, doCopy: T.(V) -> T): T {
        val newValue = modification(original)
        return if (newValue != original) {
            doCopy(newValue)
        } else this
    }
}
