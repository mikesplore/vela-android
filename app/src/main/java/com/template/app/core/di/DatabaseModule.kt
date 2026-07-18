package com.template.app.core.di

import android.content.Context
import androidx.room.Room
import com.template.app.core.data.local.AppDatabase
import com.template.app.core.data.local.LegacyConnectionMigrator
import com.template.app.core.data.local.dao.AssistantDao
import com.template.app.core.data.local.dao.PairedDeviceDao
import com.template.app.core.data.local.dao.SettingsDao
import com.template.app.core.data.local.dao.UserDao
import com.template.app.core.data.local.dao.VelaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        LegacyConnectionMigrator.captureFromLegacyDb(context)
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()

    @Provides
    @Singleton
    fun providePairedDeviceDao(db: AppDatabase): PairedDeviceDao = db.pairedDeviceDao()

    @Provides
    @Singleton
    fun provideVelaDao(db: AppDatabase): VelaDao = db.velaDao()

    @Provides
    @Singleton
    fun provideAssistantDao(db: AppDatabase): AssistantDao = db.assistantDao()
}
