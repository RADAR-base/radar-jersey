/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import org.radarbase.auth.token.RadarToken

interface Auth {
    /** Default project to apply operations to. */
    val defaultProject: String?

    val token: RadarToken

    /** ID of the OAuth client. */
    val clientId: String?
        get() = token.clientId

    /** User ID, if set in the authentication. This may be null if a client credentials grant type is used. */
    val userId: String?
        get() = token.subject?.takeUnless { it.isEmpty() }

    /**
     * Whether the current authentication is for a user with a role in given project.
     */
    fun hasRole(projectId: String, role: String): Boolean
}
