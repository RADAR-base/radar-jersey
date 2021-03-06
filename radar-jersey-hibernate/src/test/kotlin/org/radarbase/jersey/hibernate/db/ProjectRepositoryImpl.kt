package org.radarbase.jersey.hibernate.db

import org.radarbase.jersey.hibernate.HibernateRepository
import jakarta.inject.Provider
import javax.persistence.EntityManager
import jakarta.ws.rs.core.Context

class ProjectRepositoryImpl(
        @Context em: Provider<EntityManager>
): ProjectRepository, HibernateRepository(em) {
    override fun list(): List<ProjectDao> = transact {
        createQuery("SELECT p FROM Project p", ProjectDao::class.java)
                .resultList
    }

    override fun create(name: String, description: String?): ProjectDao = transact {
        ProjectDao().apply {
            this.name = name
            this.description = description
            persist(this)
        }
    }

    override fun update(id: Long, description: String?): ProjectDao? = transact {
        createQuery("SELECT p FROM Project p WHERE p.id = :id", ProjectDao::class.java)
                .apply { setParameter("id", id) }
                .resultList
                .firstOrNull()
                ?.apply {
                    this.description = description
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
