/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.exception

import jakarta.ws.rs.core.Response.Status

class HttpGatewayTimeoutException(message: String) :
    HttpApplicationException(Status.GATEWAY_TIMEOUT, "gateway_timeout", message)
