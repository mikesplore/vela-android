package com.template.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
 *   5. Bump `version` and add a Migration in DatabaseModule
 */
@Database(
    entities = [
        UserEntity::class,
        SettingsEntity::class,
        VelaHealthEntity::class,
        VelaNetworkEntity::class,
        VelaAudioEntity::class,
        VelaMediaEntity::class,
        VelaProcessEntity::class,
        VelaDiskEntity::class,
        VelaNotificationEntity::class,
        VelaWifiEntity::class,
        VelaBrightnessEntity::class,
        VelaResolutionEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    abstract fun velaDao(): VelaDao
}
