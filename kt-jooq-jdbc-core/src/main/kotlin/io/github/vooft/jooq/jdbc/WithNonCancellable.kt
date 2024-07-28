package io.github.vooft.jooq.jdbc

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal suspend fun <T> withNonCancellable(block: suspend () -> T) = withContext(coroutineContext + NonCancellable) { block() }
