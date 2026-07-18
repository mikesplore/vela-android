package com.template.app.core.device

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> ActiveConnectionProvider.scoped(
    empty: T,
    block: (connectionId: Long) -> Flow<T>
): Flow<T> = connectionId.flatMapLatest { id ->
    if (id == null) flowOf(empty) else block(id)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> ActiveConnectionProvider.scopedNullable(
    block: (connectionId: Long) -> Flow<T?>
): Flow<T?> = connectionId.flatMapLatest { id ->
    if (id == null) flowOf(null) else block(id)
}
