package org.radarbase.jersey.hibernate.config

import jakarta.persistence.SharedCacheMode
import jakarta.persistence.ValidationMode
import jakarta.persistence.spi.ClassTransformer
import jakarta.persistence.spi.PersistenceUnitInfo
import jakarta.persistence.spi.PersistenceUnitTransactionType
import org.hibernate.jpa.HibernatePersistenceProvider
import java.net.URL
import java.util.*
import javax.sql.DataSource

class RadarPersistenceInfo(
        config: DatabaseConfig
): PersistenceUnitInfo {
    @Suppress("UNCHECKED_CAST")
    private val properties: Properties = Properties().apply {
        put("jakarta.persistence.schema-generation.database.action", "none")
        put("org.hibernate.flushMode", "COMMIT")
        put("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider")
        put("hibernate.c3p0.max_size", "50")
        put("hibernate.c3p0.min_size", "0")
        put("hibernate.c3p0.acquire_increment", "1")
        put("hibernate.c3p0.idle_test_period", "300")
        put("hibernate.c3p0.max_statements", "0")
        put("hibernate.c3p0.timeout", "100")
        put("hibernate.c3p0.checkoutTimeout", "5000")
        put("hibernate.c3p0.acquireRetryAttempts", "3")
        put("hibernate.c3p0.breakAfterAcquireFailure", "false")

        sequenceOf(
            "jakarta.persistence.jdbc.driver" to config.driver,
            "jakarta.persistence.jdbc.url" to config.url,
            "jakarta.persistence.jdbc.user" to config.user,
            "jakarta.persistence.jdbc.password" to config.password,
            "hibernate.dialect" to config.dialect
        )
            .filter { (_, v) -> v != null }
            .forEach { (k, v) -> put (k, v) }

        putAll(config.properties)
    }

    private val managedClasses = config.managedClasses

    override fun getPersistenceUnitName(): String = "org.radarbase.jersey.hibernate"

    override fun getPersistenceProviderClassName(): String = HibernatePersistenceProvider::class.java.name

    override fun getTransactionType(): PersistenceUnitTransactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL

    override fun getJtaDataSource(): DataSource? = null

    override fun getNonJtaDataSource(): DataSource? = null

    override fun getMappingFileNames(): List<String> = emptyList()

    override fun getJarFileUrls(): List<URL> = emptyList()

    override fun getPersistenceUnitRootUrl(): URL? = null

    override fun getManagedClassNames(): List<String> = managedClasses

    override fun excludeUnlistedClasses(): Boolean = false

    override fun getSharedCacheMode(): SharedCacheMode = SharedCacheMode.UNSPECIFIED

    override fun getValidationMode(): ValidationMode = ValidationMode.AUTO

    override fun getProperties(): Properties = properties

    override fun getPersistenceXMLSchemaVersion(): String = "2.0"

    override fun getClassLoader(): ClassLoader = Thread.currentThread().contextClassLoader

    override fun addTransformer(transformer: ClassTransformer?) = Unit

    override fun getNewTempClassLoader(): ClassLoader? = null

    override fun toString() = "RadarPersistenceInfo(managedClasses=$managedClasses, properties=$properties)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RadarPersistenceInfo

        return properties == other.properties
                && managedClasses == other.managedClasses
    }

    override fun hashCode(): Int = Objects.hash(properties, managedClasses)
}
