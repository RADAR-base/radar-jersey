package org.radarbase.jersey.config

interface EnhancerFactory {
    fun createEnhancers(): List<JerseyResourceEnhancer>
}