package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonProperty

/** ManagementPortal Project DTO. */
data class MPProject(
        /** Project id, a name that identifies it uniquely. */
        @JsonProperty("projectName") val id: String,
        /** Project name, to be shown to users. */
        @JsonProperty("humanReadableProjectName") val name: String? = null,
        /** Where a project is organized. */
        val location: String? = null,
        /** Organization that organizes the project. */
        val organization: String? = null,
        /** Project description. */
        val description: String? = null,
        /** Any other attributes. */
        val attributes: Map<String, String> = emptyMap(),
)
