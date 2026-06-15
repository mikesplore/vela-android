package com.template.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.template.app.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VelaDao {

    @Query("SELECT * FROM vela_health WHERE id = 0")
    fun observeHealth(): Flow<VelaHealthEntity?>

    @Upsert
    suspend fun upsertHealth(health: VelaHealthEntity)

    @Query("SELECT * FROM vela_network WHERE id = 0")
    fun observeNetwork(): Flow<VelaNetworkEntity?>

    @Upsert
    suspend fun upsertNetwork(network: VelaNetworkEntity)

    @Query("SELECT * FROM vela_audio WHERE id = 0")
    fun observeAudio(): Flow<VelaAudioEntity?>

    @Upsert
    suspend fun upsertAudio(audio: VelaAudioEntity)

    @Query("SELECT * FROM vela_media WHERE id = 0")
    fun observeMedia(): Flow<VelaMediaEntity?>

    @Upsert
    suspend fun upsertMedia(media: VelaMediaEntity)

    @Query("SELECT * FROM vela_processes ORDER BY cpu DESC LIMIT :limit")
    fun observeProcesses(limit: Int): Flow<List<VelaProcessEntity>>

    @Upsert
    suspend fun upsertProcesses(processes: List<VelaProcessEntity>)

    @Query("DELETE FROM vela_processes")
    suspend fun clearProcesses()

    @Query("DELETE FROM vela_processes WHERE pid NOT IN (:pids)")
    suspend fun deleteProcessesNotIn(pids: List<Int>)

    @Transaction
    suspend fun replaceProcesses(processes: List<VelaProcessEntity>) {
        if (processes.isEmpty()) {
            clearProcesses()
        } else {
            // Upsert first to update existing ones and add new ones
            upsertProcesses(processes)
            // Then delete those that weren't in the new list to keep UI stable
            deleteProcessesNotIn(processes.map { it.pid })
        }
    }

    @Query("SELECT * FROM vela_disks")
    fun observeDisks(): Flow<List<VelaDiskEntity>>

    @Upsert
    suspend fun upsertDisks(disks: List<VelaDiskEntity>)

    @Query("DELETE FROM vela_disks")
    suspend fun clearDisks()

    @Query("DELETE FROM vela_disks WHERE mountpoint NOT IN (:mountpoints)")
    suspend fun deleteDisksNotIn(mountpoints: List<String>)

    @Transaction
    suspend fun replaceDisks(disks: List<VelaDiskEntity>) {
        if (disks.isEmpty()) {
            clearDisks()
        } else {
            upsertDisks(disks)
            deleteDisksNotIn(disks.map { it.mountpoint })
        }
    }

    @Query("SELECT * FROM vela_notifications ORDER BY timestamp DESC")
    fun observeNotifications(): Flow<List<VelaNotificationEntity>>

    @Upsert
    suspend fun upsertNotifications(notifications: List<VelaNotificationEntity>)

    @Query("DELETE FROM vela_notifications")
    suspend fun clearNotifications()

    @Transaction
    suspend fun replaceNotifications(notifications: List<VelaNotificationEntity>) {
        // Notifications are usually small enough to clear and replace
        clearNotifications()
        if (notifications.isNotEmpty()) {
            upsertNotifications(notifications)
        }
    }

    @Query("SELECT * FROM vela_wifi WHERE id = 0")
    fun observeWifi(): Flow<VelaWifiEntity?>

    @Upsert
    suspend fun upsertWifi(wifi: VelaWifiEntity)

    @Query("SELECT * FROM vela_brightness WHERE id = 0")
    fun observeBrightness(): Flow<VelaBrightnessEntity?>

    @Upsert
    suspend fun upsertBrightness(brightness: VelaBrightnessEntity)

    @Query("SELECT * FROM vela_resolution WHERE id = 0")
    fun observeResolution(): Flow<VelaResolutionEntity?>

    @Upsert
    suspend fun upsertResolution(resolution: VelaResolutionEntity)
}
