package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaAudioEntity
import com.template.app.core.data.local.entities.VelaAudioDeviceEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.device.scopedNullable
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
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : AudioRepository {

    override fun observeAudio(): Flow<VelaAudioState?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeAudio(id).map { it?.toDomain() }
        }

    override fun observeAudioDevices(): Flow<List<VelaAudioDevice>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeAudioDevices(id).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getVolume(): Resource<VelaAudioState> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getVolume()
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0,
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun setVolume(value: Int): Resource<VelaAudioState> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.setVolume(AudioVolumeRequest(value))
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0,
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun setMute(muted: Boolean): Resource<VelaAudioState> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.setMute(AudioMuteRequest(muted))
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0,
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun volumeUp(step: Int): Resource<VelaAudioState> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.volumeUp(AudioStepRequest(step))
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0,
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun volumeDown(step: Int): Resource<VelaAudioState> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.volumeDown(AudioStepRequest(step))
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        val domain = VelaAudioState(
            volume = res.volume ?: 0,
            muted = res.muted ?: false,
            micMuted = current?.micMuted ?: false,
            activeDeviceId = current?.activeDeviceId
        )
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun getAudioDevices(): Resource<List<VelaAudioDevice>> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        val domains = apiService.getAudioDevices().map {
            VelaAudioDevice(
                id = it.id ?: "",
                name = it.name ?: "Unknown Device",
                type = it.type ?: "unknown",
                isActive = it.id == current?.activeDeviceId
            )
        }
        velaDao.replaceAudioDevices(connectionId, domains.map { VelaAudioDeviceEntity.fromDomain(connectionId, it) })
        domains
    }

    override suspend fun setOutputDevice(deviceId: String): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        apiService.setOutputDevice(AudioOutputDeviceRequest(deviceId))
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, current.copy(activeDeviceId = deviceId)))
        }
        Unit
    }

    override suspend fun setMicMute(muted: Boolean): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        if (muted) apiService.disableMic() else apiService.enableMic()
        val current = velaDao.observeAudio(connectionId).firstOrNull()?.toDomain()
        if (current != null) {
            velaDao.upsertAudio(VelaAudioEntity.fromDomain(connectionId, current.copy(micMuted = muted)))
        }
        Unit
    }
}
