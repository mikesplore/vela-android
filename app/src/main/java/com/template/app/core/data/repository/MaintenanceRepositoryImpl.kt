package com.template.app.core.data.repository

import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ServiceActionRequest
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.MaintenanceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService
) : MaintenanceRepository {

    override suspend fun clearCache(): Resource<Unit> = safeApiCall {
        apiService.clearCache()
        Unit
    }

    override suspend fun getLogs(service: String, lines: Int): Resource<VelaLogs> = safeApiCall {
        val res = apiService.getLogs(service, lines)
        VelaLogs(service = res.service ?: service, lines = res.lines ?: emptyList())
    }

    override suspend fun checkUpdates(): Resource<VelaMaintenanceUpdate> = safeApiCall {
        val res = apiService.checkUpdates()
        VelaMaintenanceUpdate(
            updatesAvailable = res.updatesAvailable ?: false,
            packages = res.packages?.map { VelaPackageUpdate(it.name ?: "Unknown", it.version ?: "Unknown") } ?: emptyList()
        )
    }

    override suspend fun runUpdates(): Resource<Unit> = safeApiCall {
        apiService.runUpdates()
        Unit
    }

    override suspend fun syncTime(): Resource<Unit> = safeApiCall {
        apiService.syncTime()
        Unit
    }

    override suspend fun getServices(): Resource<List<VelaService>> = safeApiCall {
        val res = apiService.getServices()
        res.services?.map { VelaService(it.name ?: "Unknown", it.active ?: false) } ?: emptyList()
    }

    override suspend fun startService(name: String): Resource<Unit> = safeApiCall {
        apiService.startService(ServiceActionRequest(name))
        Unit
    }

    override suspend fun stopService(name: String): Resource<Unit> = safeApiCall {
        apiService.stopService(ServiceActionRequest(name))
        Unit
    }

    override suspend fun restartService(name: String): Resource<Unit> = safeApiCall {
        apiService.restartService(ServiceActionRequest(name))
        Unit
    }
}
