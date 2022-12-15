package org.radarbase.jersey.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists
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
    fun loadResources(
        factoryClass: Class<out EnhancerFactory>,
        vararg parameters: Any,
    ): ResourceConfig {
        val enhancerFactory = factoryClass
            .getConstructor(*parameters.map { it.javaClass }.toTypedArray())
            .newInstance(*parameters)
        return createResourceConfig(enhancerFactory.createEnhancers())
    }

    @JvmOverloads
    fun <T> loadConfig(
        fileName: String,
        args: Array<String>,
        clazz: Class<T>,
        mapper: ObjectMapper? = null,
    ): T = loadConfig(listOf(fileName), args, clazz, mapper)

    @JvmOverloads
    fun <T> loadConfig(
        fileNames: List<String>,
        args: Array<String>,
        clazz: Class<T>,
        mapper: ObjectMapper? = null,
    ): T {
        if ("-h" in args || "--help" in args) {
            logger.info("Usage: <command> [<config file>]")
            exitProcess(0)
        }

        val configFile = args.firstOrNull()?.let { Path(it) }
            ?: fileNames.map { Path(it) }.firstOrNull { it.exists() }
        requireNotNull(configFile) { "Configuration not provided." }

        logger.info("Reading configuration from {}", configFile.toAbsolutePath())
        return try {
            val localMapper = mapper ?: ObjectMapper(YAMLFactory())
                .registerModule(kotlinModule {
                    enable(KotlinFeature.NullToEmptyMap)
                    enable(KotlinFeature.NullToEmptyCollection)
                    enable(KotlinFeature.NullIsSameAsDefault)
                    enable(KotlinFeature.SingletonSupport)
                    enable(KotlinFeature.StrictNullChecks)
                })

            Files.newInputStream(configFile).use { input ->
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
    inline fun <reified T> loadConfig(
        fileName: String,
        args: Array<String>,
        mapper: ObjectMapper? = null,
    ): T = loadConfig(listOf(fileName), args, T::class.java, mapper)

    /**
     * Load a configuration from YAML file. The filename is searched in the current working
     * directory. This exits with a usage information message if the file cannot be loaded.
     *
     * @throws IllegalArgumentException if a file matching configFileName cannot be found
     */
    inline fun <reified T> loadConfig(
        fileNames: List<String>,
        args: Array<String>,
        mapper: ObjectMapper? = null,
    ): T = loadConfig(fileNames, args, T::class.java, mapper)

    /**
     * Create a resourceConfig based on the provided resource enhancers. This method also disables
     * the WADL since it may be identified as a security risk.
     */
    fun createResourceConfig(enhancers: List<JerseyResourceEnhancer>): ResourceConfig =
        ResourceConfig().apply {
            property("jersey.config.server.wadl.disableWadl", true)
            enhancers.forEach { enhancer ->
                packages(*enhancer.packages)
                registerClasses(*enhancer.classes)
                enhancer.enhanceResources(this@apply)
            }

            register(object : AbstractBinder() {
                override fun configure() {
                    enhancers.forEach { it.enhanceBinder(this) }
                }
            })
        }

    val logger: Logger = LoggerFactory.getLogger(ConfigLoader::class.java)

    /**
     * Perform copy if environment value with [key] is present. Otherwise, do not call [doCopy].
     * Mainly intended to use with the copy operation of data classes.
     */
    inline fun <T> T.copyEnv(
        key: String,
        doCopy: T.(String?) -> T,
    ): T = copyOnChange<T, String?>(
        original = null,
        modification = { System.getenv(key) },
        doCopy = doCopy,
    )

    /**
     * Perform copy if the [modification] function makes any change to an [original] value.
     * Mainly intended to use with data classes, where a copy is only performed if a new value is
     * present. The copy operation is used in [doCopy].
     */
    inline fun <T, V> T.copyOnChange(
        original: V,
        modification: (V) -> V,
        doCopy: T.(V) -> T,
    ): T {
        val newValue = modification(original)
        return if (newValue != original) {
            doCopy(newValue)
        } else this
    }
}
