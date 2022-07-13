package org.radarbase.jersey.hibernate.config

import jakarta.persistence.EntityTransaction
import java.io.Closeable

interface CloseableTransaction : Closeable {
    val transaction: EntityTransaction
    override fun close()
}
