package com.template.app.core.data.repository

import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.PowerProfileRequest
import com.template.app.core.data.remote.dto.ScheduleShutdownRequest
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.repository.PowerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PowerRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
) : PowerRepository {

    override suspend fun shutdown(): Resource<Unit> = safeApiCall {
        apiService.shutdown()
        Unit
    }

    override suspend fun restart(): Resource<Unit> = safeApiCall {
        apiService.restart()
        Unit
    }

    override suspend fun sleep(): Resource<Unit> = safeApiCall {
        apiService.sleep()
        Unit
    }

    override suspend fun hibernate(): Resource<Unit> = safeApiCall {
        apiService.hibernate()
        Unit
    }

    override suspend fun scheduleShutdown(at: String): Resource<Unit> = safeApiCall {
        apiService.scheduleShutdown(ScheduleShutdownRequest(at))
        Unit
    }

    override suspend fun cancelShutdown(): Resource<Unit> = safeApiCall {
        apiService.cancelShutdown(ScheduleShutdownRequest("now"))
        Unit
    }

    override suspend fun getPowerProfile(): Resource<String> = safeApiCall {
        apiService.getPowerProfile().profile ?: "unknown"
    }

    override suspend fun setPowerProfile(profile: String): Resource<Unit> = safeApiCall {
        apiService.setPowerProfile(PowerProfileRequest(profile))
        Unit
    }


}
