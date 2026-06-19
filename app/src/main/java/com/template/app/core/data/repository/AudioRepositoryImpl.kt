package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaAudioEntity
import com.template.app.core.data.local.entities.VelaAudioDeviceEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.AudioRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : AudioRepository {

    override fun observeAudio(): Flow<VelaAudioState?> =
        velaDao.observeAudio().map { it?.toDomain() }

    override fun observeAudioDevices(): Flow<List<VelaAudioDevice>> =
        velaDao.observeAudioDevices().map { list -> list.map { it.toDomain() } }

    override suspend fun getVolume(): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.getVolume()
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun setVolume(value: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setVolume(AudioVolumeRequest(value))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun setMute(muted: Boolean): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setMute(AudioMuteRequest(muted))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun volumeUp(step: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.volumeUp(AudioStepRequest(step))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun volumeDown(step: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.volumeDown(AudioStepRequest(step))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0, 
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun getAudioDevices(): Resource<List<VelaAudioDevice>> = safeApiCall {
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        val domains = apiService.getAudioDevices().map { 
            VelaAudioDevice(
                id = it.id ?: "",
                name = it.name ?: "Unknown Device",
                type = it.type ?: "unknown",
                isActive = it.id == current?.activeDeviceId
            )
        }
        velaDao.replaceAudioDevices(domains.map { VelaAudioDeviceEntity.fromDomain(it) })
        domains
    }

    override suspend fun setOutputDevice(deviceId: String): Resource<Unit> = safeApiCall {
        apiService.setOutputDevice(AudioOutputDeviceRequest(deviceId))
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertAudio(VelaAudioEntity.fromDomain(current.copy(activeDeviceId = deviceId)))
        }
        Unit
    }

    override suspend fun setMicMute(muted: Boolean): Resource<Unit> = safeApiCall {
        if (muted) apiService.disableMic() else apiService.enableMic()
        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertAudio(VelaAudioEntity.fromDomain(current.copy(micMuted = muted)))
        }
        Unit
    }
}
