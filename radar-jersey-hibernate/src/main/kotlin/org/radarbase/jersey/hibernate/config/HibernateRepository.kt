package org.radarbase.jersey.hibernate.config

import org.radarbase.jersey.exception.HttpInternalServerException
import org.slf4j.LoggerFactory
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.persistence.EntityTransaction

open class HibernateRepository(
        protected val entityManager: Provider<EntityManager>
) {
    /**
     * Run a transaction and commit it. If an exception occurs, the transaction is rolled back.
     */
    fun <T> transact(transactionOperation: EntityManager.() -> T) = createTransaction {
        it.use { transactionOperation() }
    }

    /**
     * Start a transaction without committing it. If an exception occurs, the transaction is rolled back.
     */
    private fun <T> createTransaction(transactionOperation: EntityManager.(CloseableTransaction) -> T): T {
        val entityManager = entityManager.get()
        val currentTransaction = entityManager.transaction
                ?: throw HttpInternalServerException("transaction_not_found", "Cannot find a transaction from EntityManager")

        currentTransaction.begin()
        try {
            return entityManager.transactionOperation(object : CloseableTransaction {
                override val transaction: EntityTransaction = currentTransaction

                override fun close() {
                    try {
                        transaction.commit()
                    } catch (ex: Exception) {
                        logger.error("Rolling back operation", ex)
                        if (currentTransaction.isActive) {
                            currentTransaction.rollback()
                        }
                        throw ex
                    }
                }
            })
        } catch (ex: Exception) {
            logger.error("Rolling back operation", ex)
            if (currentTransaction.isActive) {
                currentTransaction.rollback()
            }
            throw ex
        }
    }


    companion object {
        private val logger = LoggerFactory.getLogger(HibernateRepository::class.java)
    }
}

