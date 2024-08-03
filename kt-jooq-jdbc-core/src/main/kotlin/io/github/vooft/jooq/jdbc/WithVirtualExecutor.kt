package io.github.vooft.jooq.jdbc

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal val VIRTUAL_THREAD_DISPATCHER = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
