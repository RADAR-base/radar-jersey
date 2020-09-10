package org.radarbase.jersey.hibernate.config


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
)

data class LiquibaseConfig(
        val enable: Boolean = true,
        val changelogs: String = "db/changelog/changes/db.changelog-master.xml",
)
