package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaBrightnessEntity
import com.template.app.core.data.local.entities.VelaResolutionEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.DisplayRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : DisplayRepository {

    override fun observeBrightness(): Flow<VelaBrightness?> =
        velaDao.observeBrightness().map { it?.toDomain() }

    override fun observeResolution(): Flow<VelaResolution?> =
        velaDao.observeResolution().map { it?.toDomain() }

    override suspend fun getScreenshot(): Resource<String> = safeApiCall {
        apiService.getScreenshot().imageBase64 ?: ""
    }

    override suspend fun setBrightness(value: Int): Resource<Unit> = safeApiCall {
        apiService.setBrightness(BrightnessRequest(value))
        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(value)))
        Unit
    }

    override suspend fun lockDisplay(): Resource<Unit> = safeApiCall {
        apiService.lockDisplay()
        Unit
    }

    override suspend fun getResolution(): Resource<String> = safeApiCall {
        val res = apiService.getResolution()
        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
        val domain = VelaResolution(
            width = res.width ?: 0,
            height = res.height ?: 0,
            refresh = res.refresh ?: 0.0,
            output = res.output,
            rotation = current?.rotation ?: "normal",
            nightLightEnabled = current?.nightLightEnabled ?: false,
            nightLightTemp = current?.nightLightTemp ?: 4500
        )
        velaDao.upsertResolution(VelaResolutionEntity.fromDomain(domain))
        "${res.width}x${res.height} @ ${res.refresh}Hz"
    }

    override suspend fun monitorOff(): Resource<Unit> = safeApiCall {
        apiService.monitorOff()
        Unit
    }

    override suspend fun monitorOn(): Resource<Unit> = safeApiCall {
        apiService.monitorOn()
        Unit
    }

    override suspend fun rotateDisplay(orientation: String): Resource<Unit> = safeApiCall {
        apiService.rotateDisplay(RotateRequest(orientation))
        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertResolution(VelaResolutionEntity.fromDomain(current.copy(rotation = orientation)))
        }
        Unit
    }

    override suspend fun setNightLight(enabled: Boolean, temperature: Int?): Resource<Unit> = safeApiCall {
        apiService.setNightLight(NightLightRequest(enabled, temperature))
        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertResolution(VelaResolutionEntity.fromDomain(
                current.copy(
                    nightLightEnabled = enabled,
                    nightLightTemp = temperature ?: current.nightLightTemp
                )
            ))
        }
        Unit
    }

    override suspend fun recordDisplay(durationSeconds: Int): Resource<String> = safeApiCall {
        apiService.recordDisplay(RecordRequest(durationSeconds)).imageBase64 ?: ""
    }

    override suspend fun getBrightness(): Resource<Int> = safeApiCall {
        val brightness = apiService.getBrightness().brightness?.toInt() ?: 0
        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(brightness)))
        brightness
    }
}
