package io.github.vooft.jooq.jdbc

import kotlinx.coroutines.isActive
import org.jooq.DSLContext
import org.jooq.impl.DSL
import kotlin.coroutines.coroutineContext

class WithDsl(private val nonTransactionalDsl: DSLContext) {
    suspend operator fun <T> invoke(block: suspend DSLContext.() -> T): T {
        return withVirtualThreadDispatcher { nonTransactionalDsl.block() }
    }

    suspend fun <T> transactional(block: suspend DSLContext.() -> T): T {
        return withVirtualThreadDispatcher {
            val connectionProvider = nonTransactionalDsl.configuration().connectionProvider()
            val connection = requireNotNull(connectionProvider.acquire()) { "Failed to acquire connection" }
            try {
                connection.autoCommit = false
                val dsl = DSL.using(connection, nonTransactionalDsl.configuration().dialect())

                val result = dsl.block()

                if (coroutineContext.isActive) {
                    withNonCancellable { connection.commit() }
                }

                return@withVirtualThreadDispatcher result
            } catch (e: Exception) {
                withNonCancellable { connection.rollback() }
                throw e
            } finally {
                connectionProvider.release(connection)
            }
        }
    }
}
