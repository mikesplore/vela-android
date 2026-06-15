package com.template.app.core.data.remote.api

import com.template.app.core.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface VelaApiService {

    // ── General ──────────────────────────────────────────────────────────────

    @GET(".")
    suspend fun getRoot(): RootResponse

    @GET("health")
    suspend fun health(): HealthResponse

    @GET("ping")
    suspend fun ping(): PingResponse

    // ── Display ───────────────────────────────────────────────────────────────

    @GET("display/screenshot")
    suspend fun getScreenshot(): ScreenshotResponse

    @POST("display/record")
    suspend fun recordDisplay(@Body body: RecordRequest): RecordResponse

    @POST("display/monitor/off")
    suspend fun monitorOff(): GenericResponse

    @POST("display/monitor/on")
    suspend fun monitorOn(): GenericResponse

    @GET("display/brightness")
    suspend fun getBrightness(): BrightnessResponse

    @POST("display/brightness")
    suspend fun setBrightness(@Body body: BrightnessRequest): GenericResponse

    @GET("display/resolution")
    suspend fun getResolution(): ResolutionResponse

    @POST("display/resolution")
    suspend fun setResolution(@Body body: ResolutionRequest): GenericResponse

    @POST("display/rotate")
    suspend fun rotateDisplay(@Body body: RotateRequest): GenericResponse

    @POST("display/lock")
    suspend fun lockDisplay(): GenericResponse

    @POST("display/night-light")
    suspend fun setNightLight(@Body body: NightLightRequest): GenericResponse

    // ── Audio ─────────────────────────────────────────────────────────────────

    @GET("audio/volume")
    suspend fun getVolume(): AudioVolumeResponse

    @POST("audio/volume")
    suspend fun setVolume(@Body body: AudioVolumeRequest): AudioVolumeResponse

    @POST("audio/volume/up")
    suspend fun volumeUp(@Body body: AudioStepRequest): AudioVolumeResponse

    @POST("audio/volume/down")
    suspend fun volumeDown(@Body body: AudioStepRequest): AudioVolumeResponse

    @POST("audio/mute")
    suspend fun setMute(@Body body: AudioMuteRequest): AudioVolumeResponse

    @GET("audio/devices")
    suspend fun getAudioDevices(): List<AudioDevice>

    @POST("audio/output-device")
    suspend fun setOutputDevice(@Body body: AudioOutputDeviceRequest): AudioVolumeResponse

    // ── Power ─────────────────────────────────────────────────────────────────

    @POST("power/shutdown")
    suspend fun shutdown(): GenericResponse

    @POST("power/restart")
    suspend fun restart(): GenericResponse

    @POST("power/sleep")
    suspend fun sleep(): GenericResponse

    @POST("power/hibernate")
    suspend fun hibernate(): GenericResponse

    // ── Filesystem ────────────────────────────────────────────────────────────

    @GET("fs/list")
    suspend fun listFiles(@Query("path") path: String): FileListResponse

    @Streaming
    @GET("fs/download")
    suspend fun downloadFile(@Query("path") path: String): ResponseBody

    @Multipart
    @POST("fs/upload")
    suspend fun uploadFile(
        @Part("path") path: RequestBody,
        @Part file: MultipartBody.Part
    ): GenericResponse

    @DELETE("fs/delete")
    suspend fun deleteFile(@Body body: FilePathRequest): GenericResponse

    @POST("fs/mkdir")
    suspend fun makeDirectory(@Body body: FilePathRequest): GenericResponse

    @POST("fs/rename")
    suspend fun renameFile(@Body body: FileRenameRequest): GenericResponse

    @GET("fs/search")
    suspend fun searchFiles(
        @Query("query") query: String,
        @Query("path") path: String
    ): FileListResponse

    @GET("fs/disk-usage")
    suspend fun getDiskUsage(): DiskUsageResponse

    @POST("fs/zip")
    suspend fun zipFiles(@Body body: ZipRequest): GenericResponse

    @POST("fs/unzip")
    suspend fun unzipFile(@Body body: UnzipRequest): GenericResponse

    @POST("fs/open")
    suspend fun openFile(@Body body: FilePathRequest): GenericResponse

    // ── Network ───────────────────────────────────────────────────────────────

    @GET("network/ip")
    suspend fun getNetworkIp(): NetworkIpResponse

    @GET("network/location")
    suspend fun getNetworkLocation(): NetworkLocationResponse

    @GET("network/wifi/status")
    suspend fun getWifiStatus(): WifiStatusResponse

    @GET("network/wifi/list")
    suspend fun getWifiList(): WifiListResponse

    @POST("network/wifi/connect")
    suspend fun connectWifi(@Body body: WifiConnectRequest): NetworkIpResponse

    @POST("network/wifi/disconnect")
    suspend fun disconnectWifi(): NetworkIpResponse

    @POST("network/wifi/toggle")
    suspend fun toggleWifi(@Body body: WifiToggleRequest): NetworkIpResponse

    @POST("network/ping")
    suspend fun pingHost(@Body body: PingHostRequest): PingHostResponse

    @GET("network/speed-test")
    suspend fun speedTest(): SpeedTestResponse

    @GET("network/bluetooth/devices")
    suspend fun getBluetoothDevices(): BluetoothDevicesResponse

    @POST("network/bluetooth/pair")
    suspend fun pairBluetooth(@Body body: BluetoothDeviceRequest): GenericResponse

    @POST("network/bluetooth/unpair")
    suspend fun unpairBluetooth(@Body body: BluetoothDeviceRequest): GenericResponse

    // ── Notifications ─────────────────────────────────────────────────────────

    @POST("notifications/send")
    suspend fun sendNotification(@Body body: SendNotificationRequest): NotificationItem

    @POST("notifications/clear")
    suspend fun clearNotifications(): GenericResponse

    @GET("notifications/read")
    suspend fun getNotifications(): NotificationsResponse

    @GET("notifications/list")
    suspend fun listNotifications(): NotificationsResponse

    // ── Clipboard ─────────────────────────────────────────────────────────────

    @GET("clipboard/read")
    suspend fun readClipboard(): ClipboardResponse

    @POST("clipboard/write")
    suspend fun writeClipboard(@Body body: ClipboardWriteRequest): GenericResponse

    @POST("clipboard/clear")
    suspend fun clearClipboard(): GenericResponse

    // ── Media ─────────────────────────────────────────────────────────────────

    @POST("media/play-pause")
    suspend fun togglePlayPause(): GenericResponse

    @POST("media/next")
    suspend fun mediaNext(): GenericResponse

    @POST("media/previous")
    suspend fun mediaPrevious(): GenericResponse

    @POST("media/seek")
    suspend fun mediaSeek(@Body body: MediaSeekRequest): GenericResponse

    @GET("media/now-playing")
    suspend fun getNowPlaying(): MediaNowPlayingResponse

    // ── Processes ─────────────────────────────────────────────────────────────

    @GET("processes")
    suspend fun getProcesses(): ResponseBody

    @DELETE("processes/{pid}")
    suspend fun killProcessByPid(@Path("pid") pid: Int): GenericResponse

    @DELETE("processes/name/{name}")
    suspend fun killProcessByName(@Path("name") name: String): GenericResponse

    @POST("processes/launch")
    suspend fun launchProcess(@Body body: ProcessLaunchRequest): GenericResponse

    @GET("processes/active-window")
    suspend fun getActiveWindow(): ActiveWindowResponse

    @POST("processes/window/minimize")
    suspend fun minimizeWindow(@Body body: WindowActionRequest): GenericResponse

    @POST("processes/window/close")
    suspend fun closeWindow(@Body body: WindowActionRequest): GenericResponse

    // ── Input Control ─────────────────────────────────────────────────────────

    @Headers("X-Confirm-Input: true")
    @POST("input/mouse/move")
    suspend fun moveMouse(@Body body: MouseMoveRequest): GenericResponse

    @Headers("X-Confirm-Input: true")
    @POST("input/mouse/click")
    suspend fun clickMouse(@Body body: MouseClickRequest): GenericResponse

    @Headers("X-Confirm-Input: true")
    @POST("input/mouse/double-click")
    suspend fun doubleClickMouse(@Body body: MousePositionRequest): GenericResponse

    @Headers("X-Confirm-Input: true")
    @POST("input/mouse/scroll")
    suspend fun scrollMouse(@Body body: MouseScrollRequest): GenericResponse

    @Headers("X-Confirm-Input: true")
    @POST("input/keyboard/type")
    suspend fun typeText(@Body body: KeyboardTypeRequest): GenericResponse

    @Headers("X-Confirm-Input: true")
    @POST("input/keyboard/key")
    suspend fun sendKey(@Body body: KeyboardKeyRequest): GenericResponse

    // ── Security ──────────────────────────────────────────────────────────────

    @POST("security/lock")
    suspend fun securityLock(): GenericResponse

    @POST("security/logout")
    suspend fun logout(): GenericResponse

    @POST("security/webcam/disable")
    suspend fun disableWebcam(): GenericResponse

    @POST("security/webcam/enable")
    suspend fun enableWebcam(): GenericResponse

    @POST("security/webcam/snapshot")
    suspend fun webcamSnapshot(): WebcamSnapshotResponse

    @POST("security/mic/disable")
    suspend fun disableMic(): GenericResponse

    @POST("security/mic/enable")
    suspend fun enableMic(): GenericResponse

    @GET("security/login-history")
    suspend fun getLoginHistory(): LoginHistoryResponse

    @GET("security/ssh-sessions")
    suspend fun getSshSessions(): SshSessionsResponse

    // ── Scheduler ─────────────────────────────────────────────────────────────

    @POST("scheduler/create")
    suspend fun createScheduledTask(@Body body: SchedulerCreateRequest): ScheduledTask

    @GET("scheduler/list")
    suspend fun listScheduledTasks(): List<ScheduledTask>

    @DELETE("scheduler/cancel/{task_id}")
    suspend fun cancelScheduledTask(@Path("task_id") taskId: String): GenericResponse

    @POST("scheduler/run-now/{task_id}")
    suspend fun runTaskNow(@Path("task_id") taskId: String): GenericResponse

    // ── Maintenance ───────────────────────────────────────────────────────────

    @POST("maintenance/clear-cache")
    suspend fun clearCache(): GenericResponse

    @GET("maintenance/logs")
    suspend fun getLogs(
        @Query("service") service: String,
        @Query("lines") lines: Int = 100
    ): LogsResponse

    @GET("maintenance/updates")
    suspend fun checkUpdates(): UpdatesResponse

    @POST("maintenance/update")
    suspend fun runUpdates(): GenericResponse

    @POST("maintenance/sync-time")
    suspend fun syncTime(): GenericResponse

    @GET("maintenance/services")
    suspend fun getServices(): ServicesResponse

    @POST("maintenance/service/restart")
    suspend fun restartService(@Body body: ServiceActionRequest): GenericResponse

    @POST("maintenance/service/stop")
    suspend fun stopService(@Body body: ServiceActionRequest): GenericResponse

    @POST("maintenance/service/start")
    suspend fun startService(@Body body: ServiceActionRequest): GenericResponse
}
