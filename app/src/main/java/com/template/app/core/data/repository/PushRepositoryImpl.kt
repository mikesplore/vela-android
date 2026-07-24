package com.template.app.core.data.repository

import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.PushDeviceDeleteRequest
import com.template.app.core.data.remote.dto.PushDeviceRequest
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.repository.PushRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService
) : PushRepository {

    override suspend fun registerDevice(
        token: String,
        installationId: String?
    ): Resource<Unit> = safeApiCall {
        apiService.registerPushDevice(PushDeviceRequest(token = token, installationId = installationId))
        Unit
    }

    override suspend fun unregisterDevice(token: String): Resource<Unit> = safeApiCall {
        apiService.unregisterPushDevice(PushDeviceDeleteRequest(token = token))
        Unit
    }
}
