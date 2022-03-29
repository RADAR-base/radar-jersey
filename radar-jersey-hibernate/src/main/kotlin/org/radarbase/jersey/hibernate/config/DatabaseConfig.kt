package org.radarbase.jersey.hibernate.config

import org.radarbase.jersey.config.ConfigLoader.copyEnv


data class DatabaseConfig(
    /** Classes that can be used in Hibernate queries. */
    val managedClasses: List<String> = emptyList(),
    val driver: String? = "org.postgresql.Driver",
    /** JDBC URL. */
    val url: String? = null,
    /** User name. */
    val user: String? = null,
    /** User password. */
    val password: String? = null,
    /** Hibernate dialect to interact with server. */
    val dialect: String = "org.hibernate.dialect.PostgreSQLDialect",
    /** Additional hibernate properties. */
    val properties: Map<String, String> = emptyMap(),
    /** Liquibase initialization configuration. */
    val liquibase: LiquibaseConfig = LiquibaseConfig(),
    val healthCheckValiditySeconds: Long = 60
) {
    fun withEnv(): DatabaseConfig = this
        .copyEnv("DATABASE_URL") { copy(url = it) }
        .copyEnv("DATABASE_USER") { copy(user = it) }
        .copyEnv("DATABASE_PASSWORD") { copy(password = it) }
}

data class LiquibaseConfig(
    val enable: Boolean = true,
    val changelogs: String = "db/changelog/changes/db.changelog-master.xml",
    val contexts: List<String> = listOf("production"),
)
