//package com.template.app.core.data.repository
//
//import com.squareup.moshi.Moshi
//import com.template.app.core.data.local.dao.VelaDao
//import com.template.app.core.data.remote.api.VelaApiService
//import com.template.app.domain.model.*
//import com.template.app.domain.model.VelaConfig
//import kotlinx.coroutines.flow.*
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class VelaRepositoryImpl @Inject constructor(
//    private val apiService: VelaApiService,
//    private val velaDao: VelaDao,
//    private val moshi: Moshi
//) : VelaRepository {
//
//    // Headstart cache for health status
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////    // --- Actions & Refreshing ---
////
////    override suspend fun getScreenshot(): Resource<String> = safeApiCall {
////        apiService.getScreenshot().imageBase64 ?: ""
////    }
////
////    override suspend fun setBrightness(value: Int): Resource<Unit> = safeApiCall {
////        apiService.setBrightness(BrightnessRequest(value))
////        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(value)))
////        Unit
////    }
////
////    override suspend fun lockDisplay(): Resource<Unit> = safeApiCall {
////        apiService.lockDisplay()
////        Unit
////    }
////
////    override suspend fun getResolution(): Resource<String> = safeApiCall {
////        val res = apiService.getResolution()
////        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
////        val domain = VelaResolution(
////            width = res.width ?: 0,
////            height = res.height ?: 0,
////            refresh = res.refresh ?: 0.0,
////            output = res.output,
////            rotation = current?.rotation ?: "normal",
////            nightLightEnabled = current?.nightLightEnabled ?: false,
////            nightLightTemp = current?.nightLightTemp ?: 4500
////        )
////        velaDao.upsertResolution(VelaResolutionEntity.fromDomain(domain))
////        "${res.width}x${res.height} @ ${res.refresh}Hz"
////    }
////
////    override suspend fun monitorOff(): Resource<Unit> = safeApiCall {
////        apiService.monitorOff()
////        Unit
////    }
////
////    override suspend fun monitorOn(): Resource<Unit> = safeApiCall {
////        apiService.monitorOn()
////        Unit
////    }
////
////    override suspend fun rotateDisplay(orientation: String): Resource<Unit> = safeApiCall {
////        apiService.rotateDisplay(RotateRequest(orientation))
////        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
////        if (current != null) {
////            velaDao.upsertResolution(VelaResolutionEntity.fromDomain(current.copy(rotation = orientation)))
////        }
////        Unit
////    }
////
////    override suspend fun setNightLight(enabled: Boolean, temperature: Int?): Resource<Unit> = safeApiCall {
////        apiService.setNightLight(NightLightRequest(enabled, temperature))
////        val current = velaDao.observeResolution().firstOrNull()?.toDomain()
////        if (current != null) {
////            velaDao.upsertResolution(VelaResolutionEntity.fromDomain(
////                current.copy(
////                    nightLightEnabled = enabled,
////                    nightLightTemp = temperature ?: current.nightLightTemp
////                )
////            ))
////        }
////        Unit
////    }
////
////    override suspend fun recordDisplay(durationSeconds: Int): Resource<String> = safeApiCall {
////        apiService.recordDisplay(RecordRequest(durationSeconds)).imageBase64 ?: ""
////    }
////
////    override suspend fun getVolume(): Resource<VelaAudioState> = safeApiCall {
////        val res = apiService.getVolume()
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        val domain = VelaAudioState(
////            volume = res.volume ?: 0,
////            muted = res.muted ?: false,
////            micMuted = current?.micMuted ?: false,
////            activeDeviceId = current?.activeDeviceId
////        )
////        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun setVolume(value: Int): Resource<VelaAudioState> = safeApiCall {
////        val res = apiService.setVolume(AudioVolumeRequest(value))
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        val domain = VelaAudioState(
////            volume = res.volume ?: 0,
////            muted = res.muted ?: false,
////            micMuted = current?.micMuted ?: false,
////            activeDeviceId = current?.activeDeviceId
////        )
////        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun setMute(muted: Boolean): Resource<VelaAudioState> = safeApiCall {
////        val res = apiService.setMute(AudioMuteRequest(muted))
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        val domain = VelaAudioState(
////            volume = res.volume ?: 0,
////            muted = res.muted ?: false,
////            micMuted = current?.micMuted ?: false,
////            activeDeviceId = current?.activeDeviceId
////        )
////        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun volumeUp(step: Int): Resource<VelaAudioState> = safeApiCall {
////        val res = apiService.volumeUp(AudioStepRequest(step))
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        val domain = VelaAudioState(
////            volume = res.volume ?: 0,
////            muted = res.muted ?: false,
////            micMuted = current?.micMuted ?: false,
////            activeDeviceId = current?.activeDeviceId
////        )
////        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun volumeDown(step: Int): Resource<VelaAudioState> = safeApiCall {
////        val res = apiService.volumeDown(AudioStepRequest(step))
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        val domain = VelaAudioState(
////            volume = res.volume ?: 0,
////            muted = res.muted ?: false,
////            micMuted = current?.micMuted ?: false,
////            activeDeviceId = current?.activeDeviceId
////        )
////        velaDao.upsertAudio(VelaAudioEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun getAudioDevices(): Resource<List<VelaAudioDevice>> = safeApiCall {
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        val domains = apiService.getAudioDevices().map {
////            VelaAudioDevice(
////                id = it.id ?: "",
////                name = it.name ?: "Unknown Device",
////                type = it.type ?: "unknown",
////                isActive = it.id == current?.activeDeviceId
////            )
////        }
////        velaDao.replaceAudioDevices(domains.map { VelaAudioDeviceEntity.fromDomain(it) })
////        domains
////    }
////
////    override suspend fun setOutputDevice(deviceId: String): Resource<Unit> = safeApiCall {
////        apiService.setOutputDevice(AudioOutputDeviceRequest(deviceId))
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        if (current != null) {
////            velaDao.upsertAudio(VelaAudioEntity.fromDomain(current.copy(activeDeviceId = deviceId)))
////        }
////        Unit
////    }
////
////    override suspend fun setMicMute(muted: Boolean): Resource<Unit> = safeApiCall {
////        if (muted) apiService.disableMic() else apiService.enableMic()
////        val current = velaDao.observeAudio().firstOrNull()?.toDomain()
////        if (current != null) {
////            velaDao.upsertAudio(VelaAudioEntity.fromDomain(current.copy(micMuted = muted)))
////        }
////        Unit
////    }
////
////    override suspend fun shutdown(): Resource<Unit> = safeApiCall {
////        apiService.shutdown()
////        Unit
////    }
////
////    override suspend fun restart(): Resource<Unit> = safeApiCall {
////        apiService.restart()
////        Unit
////    }
////
////    override suspend fun sleep(): Resource<Unit> = safeApiCall {
////        apiService.sleep()
////        Unit
////    }
////
////    override suspend fun hibernate(): Resource<Unit> = safeApiCall {
////        apiService.hibernate()
////        Unit
////    }
////
////    override suspend fun scheduleShutdown(at: String): Resource<Unit> = safeApiCall {
////        apiService.scheduleShutdown(ScheduleShutdownRequest(at))
////        Unit
////    }
////
////    override suspend fun cancelShutdown(): Resource<Unit> = safeApiCall {
////        apiService.cancelShutdown(ScheduleShutdownRequest("now"))
////        Unit
////    }
////
////    override suspend fun getPowerProfile(): Resource<String> = safeApiCall {
////        apiService.getPowerProfile().profile ?: "unknown"
////    }
////
////    override suspend fun setPowerProfile(profile: String): Resource<Unit> = safeApiCall {
////        apiService.setPowerProfile(PowerProfileRequest(profile))
////        Unit
////    }
////
////    // --- Filesystem ---
////
////
////
////    // --- Network ---
////
////
////
////    override suspend fun getNotifications(): Resource<List<VelaNotification>> = safeApiCall {
////        val domains = apiService.getNotifications().notifications?.map {
////            VelaNotification(
////                id = it.id?.toString() ?: "",
////                title = it.title ?: "",
////                message = it.message ?: "",
////                appName = it.appName,
////                timestamp = System.currentTimeMillis()
////            )
////        } ?: emptyList()
////        velaDao.replaceNotifications(domains.map { VelaNotificationEntity.fromDomain(it) })
////        domains
////    }
////
////
////
////
////
////
////    override suspend fun getBrightness(): Resource<Int> = safeApiCall {
////        val brightness = apiService.getBrightness().brightness?.toInt() ?: 0
////        velaDao.upsertBrightness(VelaBrightnessEntity.fromDomain(VelaBrightness(brightness)))
////        brightness
////    }
////
////    override suspend fun getCpuUsage(): Resource<VelaCpuUsage> = safeApiCall {
////        val res = apiService.getMonitorCpu()
////        val domain = VelaCpuUsage(res.overall ?: 0.0, res.perCore ?: emptyList())
////        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun getRamUsage(): Resource<VelaRamUsage> = safeApiCall {
////        val res = apiService.getMonitorRam()
////        val domain = VelaRamUsage(
////            total = res.total ?: 0,
////            available = res.available ?: 0,
////            used = res.used ?: 0,
////            percent = res.percent ?: 0.0,
////            swapTotal = res.swapTotal ?: 0,
////            swapUsed = res.swapUsed ?: 0,
////            swapFree = res.swapFree ?: 0,
////            swapPercent = res.swapPercent ?: 0.0
////        )
////        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(domain))
////        domain
////    }
////
////    override suspend fun getMonitorSnapshot(): Resource<VelaMonitorSnapshot> = safeApiCall {
////        val res = apiService.getMonitorSnapshot()
////
////        val cpuDomain = VelaCpuUsage(res.cpu?.overall ?: 0.0, res.cpu?.perCore ?: emptyList())
////        val ramDomain = VelaRamUsage(
////            total = res.ram?.total ?: 0,
////            available = res.ram?.available ?: 0,
////            used = res.ram?.used ?: 0,
////            percent = res.ram?.percent ?: 0.0,
////            swapTotal = res.ram?.swapTotal ?: 0,
////            swapUsed = res.ram?.swapUsed ?: 0,
////            swapFree = res.ram?.swapFree ?: 0,
////            swapPercent = res.ram?.swapPercent ?: 0.0
////        )
////        val gpuDomains = res.gpu?.map {
////            VelaGpuUsage(it.name, it.usagePercent ?: 0.0, it.vramTotal ?: 0, it.vramUsed ?: 0, it.vramPercent ?: 0.0)
////        } ?: emptyList()
////        val diskIoDomains = res.diskIo?.map {
////            VelaDiskIo(it.device ?: "unknown", it.readBytesPerSec ?: 0.0, it.writeBytesPerSec ?: 0.0)
////        } ?: emptyList()
////        val networkIoDomains = res.networkIo?.map {
////            VelaNetworkIo(it.interfaceName ?: "unknown", it.bytesSentPerSec ?: 0.0, it.bytesRecvPerSec ?: 0.0)
////        } ?: emptyList()
////        val tempDomains = res.temperatures?.map {
////            VelaTemperatureInfo(it.sensor ?: "unknown", it.label ?: "", it.current ?: 0.0, it.high, it.critical)
////        } ?: emptyList()
////        val fanDomains = res.fans?.mapIndexed { index, it ->
////            VelaFanInfo(it.sensor ?: "unknown", it.speedRpm ?: 0, index)
////        } ?: emptyList()
////        val batteryDomain = res.battery?.let {
////            VelaBatteryStatus(it.percent ?: 0.0, it.pluggedIn ?: false, it.secsLeft)
////        }
////        val cpuProcDomains = res.processes?.topByCpu?.map { it.toDomain() } ?: emptyList()
////        val memProcDomains = res.processes?.topByMemory?.map { it.toDomain() } ?: emptyList()
////
////        // generic sensors could be added here if available in snapshot
////        val sensorDomains = emptyList<VelaSensorInfo>()
////
////        // Sync to Room
////        velaDao.upsertCpuUsage(VelaCpuUsageEntity.fromDomain(cpuDomain))
////        velaDao.upsertRamUsage(VelaRamUsageEntity.fromDomain(ramDomain))
////        velaDao.replaceGpuUsage(gpuDomains.map { VelaGpuUsageEntity.fromDomain(it) })
////        velaDao.replaceDiskIo(diskIoDomains.map { VelaDiskIoEntity.fromDomain(it) })
////        velaDao.replaceNetworkIo(networkIoDomains.map { VelaNetworkIoEntity.fromDomain(it) })
////        velaDao.replaceTemperatures(tempDomains.map { VelaTemperatureEntity.fromDomain(it) })
////        velaDao.replaceFans(fanDomains.map { VelaFanEntity.fromDomain(it) })
////        velaDao.replaceSensors(sensorDomains.map { VelaSensorEntity.fromDomain(it) })
////
////        if (batteryDomain != null) velaDao.upsertBattery(VelaBatteryEntity.fromDomain(batteryDomain))
////        velaDao.replaceCpuProcesses(cpuProcDomains.map { VelaProcessEntity.fromDomain(it, isTopByMemory = false) })
////        velaDao.replaceMemoryProcesses(memProcDomains.map { VelaProcessEntity.fromDomain(it, isTopByMemory = true) })
////
////        VelaMonitorSnapshot(
////            cpu = cpuDomain,
////            ram = ramDomain,
////            gpu = gpuDomains,
////            diskIo = diskIoDomains,
////            networkIo = networkIoDomains,
////            temperatures = tempDomains,
////            fans = fanDomains,
////            battery = batteryDomain,
////            topProcessesByCpu = cpuProcDomains,
////            topProcessesByMemory = memProcDomains,
////            sensors = sensorDomains
////        )
////    }
////
////    override suspend fun getScheduledTasks(): Resource<List<VelaScheduledTask>> = safeApiCall {
////        val response = apiService.listScheduledTasks()
////        val domains = response.jobs?.map {
////            VelaScheduledTask(
////                id = it.id ?: "",
////                command = it.command ?: "",
////                nextRun = it.nextRun ?: it.runAt ?: "Unknown",
////                recurring = it.recurring
////            )
////        } ?: emptyList()
////        velaDao.replaceScheduledTasks(domains.map { VelaScheduledTaskEntity.fromDomain(it) })
////        domains
////    }
////
////    override suspend fun createScheduledTask(
////        command: String,
////        runAt: String,
////        recurring: String?
////    ): Resource<VelaScheduledTask> = safeApiCall {
////        val res = apiService.createScheduledTask(SchedulerCreateRequest(command, runAt, recurring))
////        val domain = VelaScheduledTask(
////            id = res.id ?: "",
////            command = res.command ?: command,
////            nextRun = res.nextRun ?: runAt,
////            recurring = res.recurring ?: recurring
////        )
////        velaDao.upsertScheduledTasks(listOf(VelaScheduledTaskEntity.fromDomain(domain)))
////        domain
////    }
////
////    override suspend fun cancelScheduledTask(taskId: String): Resource<Unit> = safeApiCall {
////        apiService.cancelScheduledTask(taskId)
////        velaDao.deleteScheduledTask(taskId)
////        Unit
////    }
////
////    override suspend fun runTaskNow(taskId: String): Resource<Unit> = safeApiCall {
////        apiService.runTaskNow(taskId)
////        Unit
////    }
////
////    // --- Maintenance ---
////
////    override suspend fun clearCache(): Resource<Unit> = safeApiCall {
////        apiService.clearCache()
////        Unit
////    }
////
////    override suspend fun getLogs(service: String, lines: Int): Resource<VelaLogs> = safeApiCall {
////        val res = apiService.getLogs(service, lines)
////        VelaLogs(
////            service = res.service ?: service,
////            lines = res.lines ?: emptyList()
////        )
////    }
////
////    override suspend fun checkUpdates(): Resource<VelaMaintenanceUpdate> = safeApiCall {
////        val res = apiService.checkUpdates()
////        VelaMaintenanceUpdate(
////            updatesAvailable = res.updatesAvailable ?: false,
////            packages = res.packages?.map { VelaPackageUpdate(it.name ?: "Unknown", it.version ?: "Unknown") } ?: emptyList()
////        )
////    }
////
////    override suspend fun runUpdates(): Resource<Unit> = safeApiCall {
////        apiService.runUpdates()
////        Unit
////    }
////
////    override suspend fun syncTime(): Resource<Unit> = safeApiCall {
////        apiService.syncTime()
////        Unit
////    }
////
////    override suspend fun getServices(): Resource<List<VelaService>> = safeApiCall {
////        val res = apiService.getServices()
////        res.services?.map { VelaService(it.name ?: "Unknown", it.active ?: false) } ?: emptyList()
////    }
////
////    override suspend fun startService(name: String): Resource<Unit> = safeApiCall {
////        apiService.startService(ServiceActionRequest(name))
////        Unit
////    }
////
////    override suspend fun stopService(name: String): Resource<Unit> = safeApiCall {
////        apiService.stopService(ServiceActionRequest(name))
////        Unit
////    }
////
////    override suspend fun restartService(name: String): Resource<Unit> = safeApiCall {
////        apiService.restartService(ServiceActionRequest(name))
////        Unit
////    }
//
//
//
//
//}
