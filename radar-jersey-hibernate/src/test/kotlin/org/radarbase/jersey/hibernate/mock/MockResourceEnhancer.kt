package org.radarbase.jersey.hibernate.mock

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters
import org.radarbase.jersey.hibernate.db.ProjectRepository
import org.radarbase.jersey.hibernate.db.ProjectRepositoryImpl
import org.radarbase.jersey.service.ProjectService

class MockResourceEnhancer : JerseyResourceEnhancer {
    override val classes: Array<Class<*>> = arrayOf(
            Filters.logResponse)

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
