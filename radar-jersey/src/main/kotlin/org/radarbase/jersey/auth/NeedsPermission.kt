/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.auth

import org.radarbase.auth.authorization.Permission

/**
 * Indicates that a method needs an authenticated user that has a certain permission.
 */
@Target(AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NeedsPermission(
    /** Permission that is needed. */
    val permission: Permission,
    /** Project path parameter */
    val projectPathParam: String = "",
    /** User path parameter. */
    val userPathParam: String = "",
    /** Organization path parameter */
    val organizationPathParam: String = "",
)
