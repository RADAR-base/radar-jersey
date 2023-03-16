package org.radarbase.jersey.hibernate.db

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.jersey.hibernate.HibernateRepository

class ProjectRepositoryImpl(
        @Context em: Provider<EntityManager>
): ProjectRepository, HibernateRepository(em) {
    override fun list(): List<ProjectDao> = transact {
        createQuery("SELECT p FROM Project p", ProjectDao::class.java)
                .resultList
    }

    override fun create(name: String, description: String?, organization: String): ProjectDao = transact {
        ProjectDao().apply {
            this.name = name
            this.description = description
            this.organization = organization
            persist(this)
        }
    }

    override fun update(id: Long, description: String?, organization: String): ProjectDao? = transact {
        createQuery("SELECT p FROM Project p WHERE p.id = :id", ProjectDao::class.java)
                .apply { setParameter("id", id) }
                .resultList
                .firstOrNull()
                ?.apply {
                    this.description = description
                    this.organization = organization
                    merge(this)
                }
    }

    override fun get(id: Long): ProjectDao? = transact {
        createQuery("SELECT p FROM Project p WHERE p.id = :id", ProjectDao::class.java)
                .apply { setParameter("id", id) }
                .resultList
                .firstOrNull()
    }

    override fun delete(id: Long): Unit = transact {
        createQuery("SELECT p FROM Project p WHERE p.id = :id", ProjectDao::class.java)
                .apply { setParameter("id", id) }
                .resultList
                .firstOrNull()
                ?.apply { remove(merge(this)) }
    }
}
