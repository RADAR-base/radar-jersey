package org.radarbase.jersey.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
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

    /**
     * Load a configuration from YAML file. The filename is searched in the current working
     * directory. This exits with a usage information message if the file cannot be loaded.
     *
     * @throws IllegalArgumentException if a file matching configFileName cannot be found
     */
    inline fun <reified T> loadConfig(fileName: String, args: Array<String>): T {
        val configFileName = when {
            args.size == 1 -> args[0]
            Files.exists(Paths.get(fileName)) -> fileName
            else -> null
        }
        requireNotNull(configFileName) { "Configuration not provided." }

        val configFile = File(configFileName)
        logger.info("Reading configuration from ${configFile.absolutePath}")
        try {
            val mapper = ObjectMapper(YAMLFactory())
            return mapper.readValue(configFile, T::class.java)
        } catch (ex: IOException) {
            logger.error("Usage: <command> [$fileName]")
            logger.error("Failed to read config file $configFile: ${ex.message}")
            exitProcess(1)
        }
    }

    /**
     * Create a resourceConfig based on the provided resource enhancers. This method also disables
     * the WADL since it may be identified as a security risk.
     */
    fun createResourceConfig(enhancers: List<JerseyResourceEnhancer>): ResourceConfig {
        val resources = ResourceConfig()
        resources.property("jersey.config.server.wadl.disableWadl", true)
        enhancers.forEach { it.enhanceResources(resources) }

        resources.register(object : AbstractBinder() {
            override fun configure() {
                enhancers.forEach { it.enhanceBinder(this) }
            }
        })
        return resources
    }

    val logger: Logger = LoggerFactory.getLogger(ConfigLoader::class.java)
}
