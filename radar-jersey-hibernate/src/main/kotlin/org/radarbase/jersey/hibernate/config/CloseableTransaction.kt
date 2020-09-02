package org.radarbase.jersey.hibernate.config

import java.io.Closeable
import javax.persistence.EntityTransaction

interface CloseableTransaction : Closeable {
    val transaction: EntityTransaction
    override fun close()
}
