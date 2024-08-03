package io.github.vooft.jooq.jdbc

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import kotlin.coroutines.coroutineContext

class WithDsl(
    private val nonTransactionalDsl: DSLContext,
    private val dispatcher: CoroutineDispatcher = VIRTUAL_THREAD_DISPATCHER,
) {
    suspend operator fun <T> invoke(block: suspend DSLContext.() -> T): T =
        withContext(coroutineContext + dispatcher) { nonTransactionalDsl.block() }
}
