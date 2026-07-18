package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaScheduledTask
import kotlinx.coroutines.flow.Flow

interface SchedulesRepository {

    fun observeScheduledTasks(): Flow<List<VelaScheduledTask>>
    suspend fun getScheduledTasks(): Resource<List<VelaScheduledTask>>
    suspend fun createScheduledTask(
        command: String,
        args: List<String> = emptyList(),
        runAt: String,
        recurring: String? = null
    ): Resource<Unit>
    suspend fun cancelScheduledTask(taskId: String): Resource<Unit>
    suspend fun runTaskNow(taskId: String): Resource<Unit>
}