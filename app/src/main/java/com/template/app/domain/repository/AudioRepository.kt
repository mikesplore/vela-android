package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun observeAudio(): Flow<VelaAudioState?>
    fun observeAudioDevices(): Flow<List<VelaAudioDevice>>
    suspend fun getVolume(): Resource<VelaAudioState>
    suspend fun setVolume(value: Int): Resource<VelaAudioState>
    suspend fun setMute(muted: Boolean): Resource<VelaAudioState>
    suspend fun volumeUp(step: Int = 5): Resource<VelaAudioState>
    suspend fun volumeDown(step: Int = 5): Resource<VelaAudioState>
    suspend fun getAudioDevices(): Resource<List<VelaAudioDevice>>
    suspend fun setOutputDevice(deviceId: String): Resource<Unit>
    suspend fun setMicMute(muted: Boolean): Resource<Unit>
}
