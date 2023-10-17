package org.radarbase.jersey.hibernate.db

interface ProjectRepository {
    suspend fun list(): List<ProjectDao>
    suspend fun create(name: String, description: String?, organization: String): ProjectDao
    suspend fun update(id: Long, description: String?, organization: String): ProjectDao?
    suspend fun delete(id: Long)
    suspend fun get(id: Long): ProjectDao?
}
