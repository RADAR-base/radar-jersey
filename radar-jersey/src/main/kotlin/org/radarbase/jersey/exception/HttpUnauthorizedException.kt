/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception

import jakarta.ws.rs.core.Response

class HttpUnauthorizedException(
    code: String,
    detailedMessage: String,
    wwwAuthenticateHeader: String? = null,
    additionalHeaders: List<Pair<String, String>> = listOf(),
) : HttpApplicationException(
    Response.Status.UNAUTHORIZED,
    code,
    detailedMessage,
    additionalHeaders = buildList(additionalHeaders.size + 1) {
        if (wwwAuthenticateHeader != null) {
            add("WWW-Authenticate" to wwwAuthenticateHeader)
        }
        addAll(additionalHeaders)
    },
) {
    companion object {
        fun wwwAuthenticateHeader(
            error: String? = null,
            errorDescription: String? = null,
            scope: String? = null,
        ): String {
            return if (error == null && errorDescription == null && scope == null) {
                "Bearer realm=\"RADAR-base\""
            } else {
                buildString(30 + error.lengthOrZero + errorDescription.lengthOrZero + scope.lengthOrZero) {
                    append("Bearer realm=\"RADAR-base\"")
                    appendHeaderField("error", error)
                    appendHeaderField("error_description", errorDescription)
                    appendHeaderField("scope", scope)
                }
            }
        }

        private val headerFieldIllegalCharacters = "[^\\x20-\\x21\\x23-\\x5B\\x5D-\\x7E]".toRegex()
        private fun StringBuilder.appendHeaderField(name: String, value: String?) {
            value ?: return
            append(", ")
            append(name)
            append("=\"")
            append(value.replace(headerFieldIllegalCharacters, "?"))
            append('\"')
        }

        private val String?.lengthOrZero: Int
            get() = this?.length ?: 0
    }
}
