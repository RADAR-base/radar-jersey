/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception.mapper

import com.fasterxml.jackson.core.util.BufferRecyclers
import org.radarbase.jersey.exception.HttpApplicationException

/**
 * Render an exception using a Mustache HTML document.
 */
class DefaultJsonExceptionRenderer: ExceptionRenderer {
    override fun render(exception: HttpApplicationException): String {
        val stringEncoder = BufferRecyclers.getJsonStringEncoder()
        val quotedError = stringEncoder.quoteAsUTF8(exception.code).toString(Charsets.UTF_8)
        val quotedDescription = exception.detailedMessage?.let {
            '"' + stringEncoder.quoteAsUTF8(it).toString(Charsets.UTF_8) + '"'
        } ?: "null"

        return "{\"error\":\"$quotedError\",\"error_description\":$quotedDescription}"
    }
}
