package org.radarbase.jersey.hibernate.config

import jakarta.persistence.EntityTransaction

interface CloseableTransaction {
    val transaction: EntityTransaction
    fun commit()
    fun abort()
    fun cancel()
}
