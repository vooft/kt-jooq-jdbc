package io.github.vooft.jooq.jdbc

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import kotlin.coroutines.coroutineContext

class WithTransactionalDsl(
    private val nonTransactionalDsl: DSLContext,
    private val dispatcher: CoroutineDispatcher = VIRTUAL_THREAD_DISPATCHER,
) {
    suspend operator fun <T> invoke(block: suspend DSLContext.() -> T): T {
        return withContext(coroutineContext + dispatcher) {
            val connectionProvider = nonTransactionalDsl.configuration().connectionProvider()
            val connection = requireNotNull(connectionProvider.acquire()) { "Failed to acquire connection" }
            val initialAutoCommit = connection.autoCommit
            try {
                connection.autoCommit = false
                val dsl = DSL.using(connection, nonTransactionalDsl.configuration().dialect())

                val result = dsl.block()

                withNonCancellable { connection.commit() }

                connection.autoCommit = initialAutoCommit
                return@withContext result
            } catch (e: Exception) {
                if (!connection.isClosed) {
                    withNonCancellable {
                        connection.rollback()
                        connection.autoCommit = initialAutoCommit
                    }
                }

                throw e
            } finally {
                withNonCancellable { connectionProvider.release(connection) }
            }
        }
    }
}
