package com.template.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.dao.PairedDeviceDao
import com.template.app.core.data.local.dao.SettingsDao
import com.template.app.core.data.local.dao.UserDao
import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.*

/**
 * Central Room database. To add a new table:
 *   1. Create an Entity data class in /entities
 *   2. Create a Dao interface in /dao
 *   3. Add the entity to the `entities` array below
 *   4. Add the dao as an abstract function below
 *   5. Bump `version` (destructive migration captures legacy pair via LegacyConnectionMigrator)
 */
@Database(
    entities = [
        UserEntity::class,
        SettingsEntity::class,
        PairedDeviceEntity::class,
        VelaHealthEntity::class,
        VelaDeviceEntity::class,
        VelaNetworkEntity::class,
        VelaAudioEntity::class,
        VelaAudioDeviceEntity::class,
        VelaMediaEntity::class,
        VelaProcessEntity::class,
        VelaDiskEntity::class,
        VelaNotificationEntity::class,
        VelaWifiEntity::class,
        VelaBrightnessEntity::class,
        VelaResolutionEntity::class,
        VelaCpuUsageEntity::class,
        VelaRamUsageEntity::class,
        VelaGpuUsageEntity::class,
        VelaDiskIoEntity::class,
        VelaNetworkIoEntity::class,
        VelaTemperatureEntity::class,
        VelaSensorEntity::class,
        VelaFanEntity::class,
        VelaBatteryEntity::class,
        VelaClipboardEntity::class,
        VelaActiveWindowEntity::class,
        VelaScheduledTaskEntity::class,
        VelaServiceEntity::class,
        VelaFileEntity::class,
        VelaConfigEntity::class,
        VelaWifiNetworkEntity::class,
        VelaBluetoothEntity::class,
        VelaBluetoothDeviceEntity::class,
        AssistantMessageEntity::class,
        VelaUptimeEntity::class,
        NetUsageEntity::class
    ],
    version = 28,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    abstract fun pairedDeviceDao(): PairedDeviceDao
    abstract fun velaDao(): VelaDao
    abstract fun assistantDao(): AssistantDao
}
