package org.radarbase.jersey.hibernate.mock

import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.config.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.db.ProjectRepository
import org.radarbase.jersey.hibernate.db.ProjectRepositoryImpl
import org.radarbase.jersey.service.ProjectService
import jakarta.inject.Singleton

class MockResourceEnhancer : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            ConfigLoader.Filters.logResponse)

    override val packages: Array<String> = arrayOf(
            "org.radarbase.jersey.hibernate.mock.resource")

    override fun AbstractBinder.enhance() {
        bind(ProjectRepositoryImpl::class.java)
                .to(ProjectRepository::class.java)
                .`in`(Singleton::class.java)

        bind(MockProjectService::class.java)
                .to(ProjectService::class.java)
                .`in`(Singleton::class.java)
    }
}
