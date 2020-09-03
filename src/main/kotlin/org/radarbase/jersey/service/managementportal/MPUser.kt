package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonProperty

data class MPUser(
        @JsonProperty("login") val id: String,
        val projectId: String? = null,
        val externalId: String? = null,
        val status: String = "DEACTIVATED",
        val attributes: Map<String, String> = mapOf())
