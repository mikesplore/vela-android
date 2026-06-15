package com.template.app.core.di

import android.content.Context
import androidx.room.Room
import com.template.app.core.data.local.AppDatabase
import com.template.app.core.data.local.dao.SettingsDao
import com.template.app.core.data.local.dao.UserDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            // Add migrations here when you bump the version in AppDatabase
            // .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)   // Remove in production; use real migrations
            .build()

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideSettingsDao(db: AppDatabase): SettingsDao = db.settingsDao()
}
