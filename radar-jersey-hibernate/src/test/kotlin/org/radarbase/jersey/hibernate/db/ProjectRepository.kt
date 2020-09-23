package org.radarbase.jersey.hibernate.db

interface ProjectRepository {
    fun list(): List<ProjectDao>
    fun create(name: String, description: String?): ProjectDao
    fun update(id: Long, description: String?): ProjectDao?
    fun delete(id: Long)
    fun get(id: Long): ProjectDao?
}
