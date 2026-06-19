package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaProcess
import kotlinx.coroutines.flow.Flow

interface ProcessesRepository {
    fun observeActiveWindow(): Flow<String?>

    fun observeProcesses(limit: Int = 5): Flow<List<VelaProcess>>

    suspend fun getProcesses(): Resource<List<VelaProcess>>
    suspend fun getActiveWindow(): Resource<String>
    suspend fun killProcess(pid: Int): Resource<Unit>
}