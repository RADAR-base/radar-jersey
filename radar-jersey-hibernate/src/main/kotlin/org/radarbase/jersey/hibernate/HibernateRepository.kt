package org.radarbase.jersey.hibernate

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityTransaction
import kotlinx.coroutines.*
import org.glassfish.jersey.process.internal.RequestScope
import org.hibernate.Session
import org.radarbase.jersey.exception.HttpInternalServerException
import org.radarbase.jersey.hibernate.config.CloseableTransaction
import org.slf4j.LoggerFactory
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class HibernateRepository(
    private val entityManagerProvider: Provider<EntityManager>,
    private val requestScope: RequestScope,
) {
    @Suppress("MemberVisibilityCanBePrivate")
    protected val entityManager: EntityManager
        get() = entityManagerProvider.get()

    /**
     * Run a transaction and commit it. If an exception occurs, the transaction is rolled back.
     */
    suspend fun <T> transact(transactionOperation: EntityManager.() -> T): T = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val storedTransaction = AtomicReference<CloseableTransaction?>(null)
            continuation.invokeOnCancellation { storedTransaction.get()?.cancel() }
            try {
                continuation.resume(
                    createTransaction { transaction ->
                        storedTransaction.set(transaction)
                        try {
                            val result = transactionOperation()
                            transaction.commit()
                            return@createTransaction result
                        } catch (ex: Throwable) {
                            logger.warn("Rolling back failed operation: {}", ex.toString())
                            transaction.abort()
                            throw ex
                        } finally {
                            storedTransaction.set(null)
                        }
                    }
                )
            } catch (ex: Throwable) {
                continuation.resumeWithException(ex)
            }
        }
    }

    /**
     * Start a transaction without committing it. If an exception occurs, the transaction is rolled back.
     */
    fun <T> createTransaction(
        transactionOperation: EntityManager.(CloseableTransaction) -> T,
    ): T = requestScope.runInScope(
        Callable {
            val em = entityManager
            val session = em.unwrap(Session::class.java)
                ?: throw HttpInternalServerException("session_not_found", "Cannot find a session from EntityManager")
            val suspendTransaction = SuspendableCloseableTransaction(session)
            try {
                suspendTransaction.begin()
                em.transactionOperation(suspendTransaction)
            } catch (ex: Exception) {
                logger.error("Rolling back operation", ex)
                suspendTransaction.abort()
                throw ex
            }
        }
    )

    companion object {
        private val logger = LoggerFactory.getLogger(HibernateRepository::class.java)
        private class SuspendableCloseableTransaction(
            private val session: Session,
        ) : CloseableTransaction {

            override val transaction: EntityTransaction = session.transaction
                ?: throw HttpInternalServerException("transaction_not_found", "Cannot find a transaction from EntityManager")

            fun begin() {
                transaction.begin()
            }

            override fun abort() {
                if (transaction.isActive) {
                    transaction.rollback()
                }
            }

            override fun commit() {
                transaction.commit()
            }

            override fun cancel() {
                session.cancelQuery()
            }
        }
    }
}
