package com.template.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
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

    @Query("SELECT * FROM vela_processes ORDER BY cpu DESC")
    fun observeProcesses(): Flow<List<VelaProcessEntity>>

    @Upsert
    suspend fun upsertProcesses(processes: List<VelaProcessEntity>)

    @Query("DELETE FROM vela_processes")
    suspend fun clearProcesses()

    @Query("SELECT * FROM vela_disks")
    fun observeDisks(): Flow<List<VelaDiskEntity>>

    @Upsert
    suspend fun upsertDisks(disks: List<VelaDiskEntity>)

    @Query("DELETE FROM vela_disks")
    suspend fun clearDisks()
}
