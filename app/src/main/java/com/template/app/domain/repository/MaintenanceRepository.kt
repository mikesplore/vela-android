package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaLogs
import com.template.app.domain.model.VelaMaintenanceUpdate
import com.template.app.domain.model.VelaService

interface MaintenanceRepository {
    // Maintenance
    suspend fun clearCache(): Resource<Unit>
    suspend fun getLogs(service: String, lines: Int): Resource<VelaLogs>
    suspend fun checkUpdates(): Resource<VelaMaintenanceUpdate>
    suspend fun runUpdates(): Resource<Unit>
    suspend fun syncTime(): Resource<Unit>
    suspend fun getServices(): Resource<List<VelaService>>
    suspend fun startService(name: String): Resource<Unit>
    suspend fun stopService(name: String): Resource<Unit>
    suspend fun restartService(name: String): Resource<Unit>
}