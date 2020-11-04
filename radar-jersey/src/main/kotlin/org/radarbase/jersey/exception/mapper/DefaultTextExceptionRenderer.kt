/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception.mapper

import org.radarbase.jersey.exception.HttpApplicationException

/**
 * Render an exception using a Mustache HTML document.
 */
class DefaultTextExceptionRenderer: ExceptionRenderer {
    override fun render(exception: HttpApplicationException): String {
        return "[${exception.status}] ${exception.code}: ${exception.detailedMessage ?: "unknown reason"}"
    }
}
