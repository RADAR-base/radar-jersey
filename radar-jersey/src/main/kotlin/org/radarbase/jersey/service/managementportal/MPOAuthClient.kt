package org.radarbase.jersey.service.managementportal

import com.fasterxml.jackson.annotation.JsonProperty

data class MPOAuthClient(
        @JsonProperty("clientId") val id: String,
)
