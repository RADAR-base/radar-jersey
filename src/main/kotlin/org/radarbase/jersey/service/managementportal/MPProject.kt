package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonProperty

data class MPProject(
        @JsonProperty("projectName") val id: String,
        @JsonProperty("humanReadableProjectName") val name: String? = null,
        val location: String? = null,
        val organization: String? = null,
        val description: String? = null,
        val attributes: Map<String, String> = emptyMap())
