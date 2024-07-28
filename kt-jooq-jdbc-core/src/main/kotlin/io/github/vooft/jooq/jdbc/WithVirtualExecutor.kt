package io.github.vooft.jooq.jdbc

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext

private val VIRTUAL_THREAD_DISPATCHER = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

internal suspend fun <T> withVirtualThreadDispatcher(block: suspend () -> T) = withContext(coroutineContext + VIRTUAL_THREAD_DISPATCHER) {
    block()
}
