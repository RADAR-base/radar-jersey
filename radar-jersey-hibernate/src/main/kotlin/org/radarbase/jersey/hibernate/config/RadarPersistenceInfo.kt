package org.radarbase.jersey.hibernate.config

import org.hibernate.jpa.HibernatePersistenceProvider
import java.net.URL
import java.util.*
import javax.persistence.SharedCacheMode
import javax.persistence.ValidationMode
import javax.persistence.spi.ClassTransformer
import javax.persistence.spi.PersistenceUnitInfo
import javax.persistence.spi.PersistenceUnitTransactionType
import javax.sql.DataSource

class RadarPersistenceInfo(
        config: DatabaseConfig
): PersistenceUnitInfo {
    @Suppress("UNCHECKED_CAST")
    private val properties: Properties = Properties().apply {
        put("javax.persistence.schema-generation.database.action", "none")
        put("org.hibernate.flushMode", "COMMIT")
        put("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider")
        put("hibernate.c3p0.max_size", "50")
        put("hibernate.c3p0.min_size", "0")
        put("hibernate.c3p0.acquire_increment", "1")
        put("hibernate.c3p0.idle_test_period", "300")
        put("hibernate.c3p0.max_statements", "0")
        put("hibernate.c3p0.timeout", "100")
        val additionalProperties: Map<String, String> = (mapOf(
                "javax.persistence.jdbc.driver" to config.driver,
                "javax.persistence.jdbc.url" to config.url,
                "javax.persistence.jdbc.user" to config.user,
                "javax.persistence.jdbc.password" to config.password,
                "hibernate.dialect" to config.dialect)
                + config.properties)
                .filterValues { it != null } as Map<String, String>

        putAll(additionalProperties)
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

    override fun toString(): String {
        return "RadarPersistenceInfo(properties=$properties)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RadarPersistenceInfo

        return properties == other.properties
    }

    override fun hashCode(): Int = properties.hashCode()
}
