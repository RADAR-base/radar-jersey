package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonProperty

/** ManagementPortal Subject DTO. */
data class MPUser(
        /** User id, a name that identifies it uniquely. */
        @JsonProperty("login") val id: String,
        /** Project id that the user belongs to. */
        val projectId: String? = null,
        /** ID in an external system for the user. */
        val externalId: String? = null,
        /** User status in the project. */
        val status: String = "DEACTIVATED",
        /** Additional attributes of the user. */
        val attributes: Map<String, String> = emptyMap())
