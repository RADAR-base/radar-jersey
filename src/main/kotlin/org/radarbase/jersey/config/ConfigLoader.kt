package org.radarbase.jersey.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.glassfish.jersey.server.ResourceConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object ConfigLoader {
    inline fun <reified T> loadResources(factoryClass: Class<*>, config: T): ResourceConfig {
        val enhancerFactory = factoryClass.getConstructor(T::class.java).newInstance(config) as EnhancerFactory
        return RadarResourceConfigFactory().resources(enhancerFactory.createEnhancers())
    }

    inline fun <reified T> loadConfig(fileName: String, args: Array<String>): T {
        val configFileName = when {
            args.size == 1 -> args[0]
            Files.exists(Paths.get(fileName)) -> fileName
            else -> null
        }
        checkNotNull(configFileName) { "Configuration not provided." }

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

    val logger: Logger = LoggerFactory.getLogger(ConfigLoader::class.java)
}
