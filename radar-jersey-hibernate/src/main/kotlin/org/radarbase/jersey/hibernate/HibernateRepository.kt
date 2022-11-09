package org.radarbase.jersey.hibernate

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityTransaction
import org.radarbase.jersey.exception.HttpInternalServerException
import org.radarbase.jersey.hibernate.config.CloseableTransaction
import org.slf4j.LoggerFactory

open class HibernateRepository(
        private val entityManagerProvider: Provider<EntityManager>
) {
    @Suppress("MemberVisibilityCanBePrivate")
    protected val entityManager: EntityManager
        get() = entityManagerProvider.get()

    @Suppress("unused")
    fun <T> transact(transactionOperation: EntityManager.() -> T) = entityManager.transact(transactionOperation)
    @Suppress("unused")
    fun <T> createTransaction(transactionOperation: EntityManager.(CloseableTransaction) -> T) = entityManager.createTransaction(transactionOperation)

    /**
     * Run a transaction and commit it. If an exception occurs, the transaction is rolled back.
     */
    open fun <T> EntityManager.transact(transactionOperation: EntityManager.() -> T) = createTransaction {
        it.use { transactionOperation() }
    }

    /**
     * Start a transaction without committing it. If an exception occurs, the transaction is rolled back.
     */
    open fun <T> EntityManager.createTransaction(transactionOperation: EntityManager.(CloseableTransaction) -> T): T {
        val currentTransaction = transaction
                ?: throw HttpInternalServerException("transaction_not_found", "Cannot find a transaction from EntityManager")

        currentTransaction.begin()
        try {
            return transactionOperation(object : CloseableTransaction {
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

