package org.radarbase.jersey.hibernate.config


data class DatabaseConfig(
        val driver: String? = "org.postgresql.Driver",
        val url: String? = null,
        val user: String? = null,
        val password: String? = null,
        val dialect: String = "org.hibernate.dialect.PostgreSQLDialect",
        val managedClasses: List<String> = emptyList(),
        val properties: Map<String, String> = emptyMap())
