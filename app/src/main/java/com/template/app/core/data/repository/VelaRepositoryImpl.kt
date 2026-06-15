package com.template.app.core.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.VelaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VelaRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val moshi: Moshi
) : VelaRepository {

    override fun observeHealth(): Flow<VelaHealth?> =
        velaDao.observeHealth().map { it?.toDomain() }

    override fun observeNetwork(): Flow<VelaNetworkInfo?> =
        velaDao.observeNetwork().map { it?.toDomain() }

    override fun observeAudio(): Flow<VelaAudioState?> =
        velaDao.observeAudio().map { it?.toDomain() }

    override fun observeMedia(): Flow<VelaMediaState?> =
        velaDao.observeMedia().map { it?.toDomain() }

    override fun observeProcesses(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcesses(limit).map { list -> list.map { it.toDomain() } }

    override fun observeDisks(): Flow<List<VelaDiskUsage>> =
        velaDao.observeDisks().map { list -> list.map { it.toDomain() } }

    override fun observeNotifications(): Flow<List<VelaNotification>> =
        velaDao.observeNotifications().map { list -> list.map { it.toDomain() } }

    override fun observeWifi(): Flow<VelaWifiStatus?> =
        velaDao.observeWifi().map { it?.toDomain() }

    override fun observeBrightness(): Flow<VelaBrightness?> =
        velaDao.observeBrightness().map { it?.toDomain() }

    override fun observeResolution(): Flow<VelaResolution?> =
        velaDao.observeResolution().map { it?.toDomain() }

    // --- Actions & Refreshing ---

    override suspend fun getHealth(): Resource<VelaHealth> = safeApiCall {
        val response = apiService.health()
        val domain = VelaHealth(
            status = response.status ?: "unknown",
            uptimeSeconds = response.uptimeSeconds ?: 0L
        )
        velaDao.upsertHealth(VelaHealthEntity.fromDomain(domain))
        domain
    }

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
        val domain = VelaResolution(
            width = res.width ?: 0,
            height = res.height ?: 0,
            refresh = res.refresh ?: 0.0,
            output = res.output
        )
        velaDao.upsertResolution(VelaResolutionEntity.fromDomain(domain))
        "${res.width}x${res.height} @ ${res.refresh}Hz"
    }

    override suspend fun getVolume(): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.getVolume()
        val domain = VelaAudioState(res.volume ?: 0, res.muted ?: false)
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun setVolume(value: Int): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setVolume(AudioVolumeRequest(value))
        val domain = VelaAudioState(res.volume ?: 0, res.muted ?: false)
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
    }

    override suspend fun setMute(muted: Boolean): Resource<VelaAudioState> = safeApiCall {
        val res = apiService.setMute(AudioMuteRequest(muted))
        val domain = VelaAudioState(res.volume ?: 0, res.muted ?: false)
        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
        domain
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
        val domains = apiService.getDiskUsage().usage?.map {
            VelaDiskUsage(
                mountpoint = it.mountpoint ?: "",
                total = it.total ?: 0L,
                used = it.used ?: 0L,
                free = it.free ?: 0L,
                percent = it.percent ?: 0.0
            )
        } ?: emptyList()
        velaDao.replaceDisks(domains.map { VelaDiskEntity.fromDomain(it) })
        domains
    }

    override suspend fun getNetworkInfo(): Resource<VelaNetworkInfo> = safeApiCall {
        val response = apiService.getNetworkIp()
        val domain = VelaNetworkInfo(
            localIp = response.localIp ?: "",
            publicIp = response.publicIp ?: "",
            interfaceName = response.interfaceName ?: ""
        )
        velaDao.upsertNetwork(VelaNetworkEntity.fromDomain(domain))
        domain
    }

    override suspend fun getWifiStatus(): Resource<String> = safeApiCall {
        val res = apiService.getWifiStatus()
        val domain = VelaWifiStatus(
            connected = res.connected ?: false,
            ssid = res.ssid,
            signal = res.signal
        )
        velaDao.upsertWifi(VelaWifiEntity.fromDomain(domain))
        res.ssid ?: "Unknown"
    }

    override suspend fun getNotifications(): Resource<List<VelaNotification>> = safeApiCall {
        val domains = apiService.getNotifications().notifications?.map {
            VelaNotification(
                id = it.id?.toString() ?: "",
                title = it.title ?: "",
                message = it.message ?: "",
                appName = it.appName,
                timestamp = System.currentTimeMillis()
            )
        } ?: emptyList()
        velaDao.replaceNotifications(domains.map { VelaNotificationEntity.fromDomain(it) })
        domains
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
            val domain = VelaMediaState(
                title = it.title,
                artist = it.artist,
                album = it.album,
                status = it.status,
                positionSeconds = it.positionSeconds,
                lengthSeconds = it.lengthSeconds
            )
            velaDao.upsertMedia(VelaMediaEntity.fromDomain(domain))
            domain
        }
    }

    override suspend fun togglePlayPause(): Resource<Unit> = safeApiCall {
        apiService.togglePlayPause()
        Unit
    }

    override suspend fun getProcesses(): Resource<List<VelaProcess>> = safeApiCall {
        val jsonStr = apiService.getProcesses().string()
        val domains = parseProcessesResiliently(jsonStr)
        velaDao.replaceProcesses(domains.map { VelaProcessEntity.fromDomain(it) })
        domains
    }

    override suspend fun getActiveWindow(): Resource<String> = safeApiCall {
        apiService.getActiveWindow().title ?: ""
    }

    override suspend fun getBrightness(): Resource<Int> = safeApiCall {
        val brightness = apiService.getBrightness().brightness?.toInt() ?: 0
        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(brightness)))
        brightness
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
