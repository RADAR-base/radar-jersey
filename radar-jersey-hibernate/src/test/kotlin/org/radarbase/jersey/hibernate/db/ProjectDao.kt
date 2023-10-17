package org.radarbase.jersey.hibernate.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table

@Entity(name = "Project")
@Table(name = "project")
class ProjectDao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "project_id_seq", initialValue = 1, allocationSize = 1)
    var id: Long? = null

    @Column
    var name: String = ""

    @Column
    var description: String? = null

    @Column
    var organization: String? = null
}
