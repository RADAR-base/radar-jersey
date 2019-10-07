/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.config

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

interface JerseyResourceEnhancer {
    fun enhanceResources(resourceConfig: ResourceConfig) = Unit
    fun enhanceBinder(binder: AbstractBinder) = Unit
}
