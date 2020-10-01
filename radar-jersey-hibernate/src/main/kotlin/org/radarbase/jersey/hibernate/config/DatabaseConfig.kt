package org.radarbase.jersey.hibernate.config

import org.radarbase.jersey.config.letEnv


data class DatabaseConfig(
        /** Classes that can be used in Hibernate queries. */
        val managedClasses: List<String> = emptyList(),
        val driver: String? = "org.postgresql.Driver",
        val url: String? = null,
        val user: String? = null,
        val password: String? = null,
        val dialect: String = "org.hibernate.dialect.PostgreSQLDialect",
        val properties: Map<String, String> = emptyMap(),
        val liquibase: LiquibaseConfig = LiquibaseConfig(),
        val healthCheckValiditySeconds: Long = 60
) {
    fun combineWithEnv(): DatabaseConfig = this
            .letEnv("DATABASE_URL") { copy(url = it) }
            .letEnv("DATABASE_USER") { copy(user = it) }
            .letEnv("DATABASE_PASSWORD") { copy(password = it) }
}

data class LiquibaseConfig(
        val enable: Boolean = true,
        val changelogs: String = "db/changelog/changes/db.changelog-master.xml",
)
