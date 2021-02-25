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

class HttpInternalServerException(code: String, message: String) :
        HttpApplicationException(Response.Status.INTERNAL_SERVER_ERROR, code, message)
