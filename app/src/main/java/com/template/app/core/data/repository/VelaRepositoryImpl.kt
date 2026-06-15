package com.template.app.core.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.VelaRepository
import javax.inject.Inject

class VelaRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val moshi: Moshi
) : VelaRepository {

    override suspend fun getHealth(): Resource<VelaHealth> = safeApiCall {
        val response = apiService.health()
        VelaHealth(
            status = response.status ?: "unknown",
            uptimeSeconds = response.uptimeSeconds ?: 0L
        )
    }

    override suspend fun getScreenshot(): Resource<String> = safeApiCall {
        apiService.getScreenshot().imageBase64 ?: ""
    }

    override suspend fun setBrightness(value: Int): Resource<Unit> = safeApiCall {
        apiService.setBrightness(BrightnessRequest(value))
        Unit
    }

    override suspend fun lockDisplay(): Resource<Unit> = safeApiCall {
        apiService.lockDisplay()
        Unit
    }

    override suspend fun getResolution(): Resource<String> = safeApiCall {
        val res = apiService.getResolution()
        "${res.width}x${res.height} @ ${res.refresh}Hz"
    }

    override suspend fun getVolume(): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.getVolume()
        VelaAudioState(res.volume ?: 0, res.muted ?: false)
    }

    override suspend fun setVolume(value: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setVolume(AudioVolumeRequest(value))
        VelaAudioState(res.volume ?: 0, res.muted ?: false)
    }

    override suspend fun setMute(muted: Boolean): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setMute(AudioMuteRequest(muted))
        VelaAudioState(res.volume ?: 0, res.muted ?: false)
    }

    override suspend fun shutdown(): Resource<Unit> = safeApiCall {
        apiService.shutdown()
        Unit
    }

    override suspend fun listFiles(path: String): Resource<List<VelaFileInfo>> = safeApiCall {
        apiService.listFiles(path).files?.map {
            VelaFileInfo(
                name = it.name ?: "",
                path = it.path ?: "",
                type = it.type ?: "",
                size = it.size ?: 0L,
                modified = it.modified ?: 0L
            )
        } ?: emptyList()
    }

    override suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>> = safeApiCall {
        apiService.getDiskUsage().usage?.map {
            VelaDiskUsage(
                mountpoint = it.mountpoint ?: "",
                total = it.total ?: 0L,
                used = it.used ?: 0L,
                free = it.free ?: 0L,
                percent = it.percent ?: 0.0
            )
        } ?: emptyList()
    }

    override suspend fun getNetworkInfo(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkIp()
        VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp ?: "",
            interfaceName = response.interfaceName ?: ""
        )
    }

    override suspend fun getWifiStatus(): Resource<String> = safeApiCall {
        apiService.getWifiStatus().ssid ?: "Unknown"
    }

    override suspend fun getNotifications(): Resource<List<VelaNotification>> = safeApiCall {
        apiService.getNotifications().notifications?.map {
            VelaNotification(
                id = it.id?.toString() ?: "",
                title = it.title ?: "",
                message = it.message ?: "",
                appName = it.appName,
                timestamp = System.currentTimeMillis() // Fallback
            )
        } ?: emptyList()
    }

    override suspend fun readClipboard(): Resource<String> = safeApiCall {
        apiService.readClipboard().data ?: ""
    }

    override suspend fun writeClipboard(text: String): Resource<Unit> = safeApiCall {
        apiService.writeClipboard(ClipboardWriteRequest(text))
        Unit
    }

    override suspend fun getNowPlaying(): Resource<VelaMediaState?> = safeApiCall {
        apiService.getNowPlaying().let {
            VelaMediaState(
                title = it.title,
                artist = it.artist,
                album = it.album,
                status = it.status,
                positionSeconds = it.positionSeconds,
                lengthSeconds = it.lengthSeconds
            )
        }
    }

    override suspend fun togglePlayPause(): Resource<Unit> = safeApiCall {
        apiService.togglePlayPause()
        Unit
    }

    override suspend fun getProcesses(): Resource<List<VelaProcess>> = safeApiCall {
        val jsonStr = apiService.getProcesses().string()
        parseProcessesResiliently(jsonStr)
    }

    override suspend fun getActiveWindow(): Resource<String> = safeApiCall {
        apiService.getActiveWindow().title ?: ""
    }

    override suspend fun getBrightness(): Resource<Int> = safeApiCall {
        apiService.getBrightness().brightness?.toInt() ?: 0
    }

    private fun parseProcessesResiliently(jsonStr: String): List<VelaProcess> {
        try {
            val listType = Types.newParameterizedType(List::class.java, ProcessItem::class.java)
            val adapter = moshi.adapter<List<ProcessItem>>(listType)
            val list = adapter.fromJson(jsonStr)
            if (list != null) return list.map { it.toDomain() }
        } catch (e: Exception) {}

        try {
            val adapter = moshi.adapter(ProcessesResponse::class.java)
            val obj = adapter.fromJson(jsonStr)
            if (obj?.processes != null) return obj.processes.map { it.toDomain() }
        } catch (e: Exception) {}

        try {
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)
            val parsedMap = adapter.fromJson(jsonStr)
            if (parsedMap != null) {
                for ((_, value) in parsedMap) {
                    if (value is List<*>) {
                        val subJson = moshi.adapter(Any::class.java).toJson(value)
                        val listType = Types.newParameterizedType(List::class.java, ProcessItem::class.java)
                        val list = moshi.adapter<List<ProcessItem>>(listType).fromJson(subJson)
                        if (list != null) return list.map { it.toDomain() }
                    }
                }
            }
        } catch (e: Exception) {}

        return emptyList()
    }

    private fun ProcessItem.toDomain() = VelaProcess(
        pid = pid ?: 0,
        name = name ?: "Unknown",
        cpu = cpu ?: 0.0,
        mem = mem ?: 0.0
    )
}
