package org.radarbase.jersey.exception.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    val error: String,
    @param:JsonProperty("error_description")
    val description: String,
)
