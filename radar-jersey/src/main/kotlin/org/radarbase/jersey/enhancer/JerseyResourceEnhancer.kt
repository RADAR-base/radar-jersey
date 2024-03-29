/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.jersey.enhancer

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig

/**
 * Enhance a Jersey ResourceConfig and a binder. This is used for dependency injection with
 * {@code @Context}.
 */
interface JerseyResourceEnhancer {
    val classes: Array<Class<*>>
        get() = emptyArray()

    val packages: Array<String>
        get() = emptyArray()

    fun AbstractBinder.enhance() = Unit

    fun ResourceConfig.enhance() = Unit

    /**
     * Enhance the ResourceConfig directly. Use this for classes with Jersey-recognized classes like
     * {@code @Resource}, {@code @Provider} or {@code ContextResolver}.
     */
    fun enhanceResources(resourceConfig: ResourceConfig) = resourceConfig.enhance()

    /**
     * Enhance an AbstractBinder. Use this for app-specific bindings.
     */
    fun enhanceBinder(binder: AbstractBinder) = binder.enhance()
}
