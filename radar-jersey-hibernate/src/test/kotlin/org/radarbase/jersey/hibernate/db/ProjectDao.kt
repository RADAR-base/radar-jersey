package org.radarbase.jersey.hibernate.db

import jakarta.persistence.*

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
