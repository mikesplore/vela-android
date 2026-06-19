package com.template.app.core.di

import com.template.app.core.data.repository.AudioRepositoryImpl
import com.template.app.core.data.repository.ClipboardRepositoryImpl
import com.template.app.core.data.repository.ConfigRepositoryImpl
import com.template.app.core.data.repository.DisplayRepositoryImpl
import com.template.app.core.data.repository.FilesystemRepositoryImpl
import com.template.app.core.data.repository.HealthRepositoryImpl
import com.template.app.core.data.repository.MaintenanceRepositoryImpl
import com.template.app.core.data.repository.MediaRepositoryImpl
import com.template.app.core.data.repository.MonitorRepositoryImpl
import com.template.app.core.data.repository.NetworkRepositoryImpl
import com.template.app.core.data.repository.NotificationsRepositoryImpl
import com.template.app.core.data.repository.PowerRepositoryImpl
import com.template.app.core.data.repository.ProcessesRepositoryImpl
import com.template.app.core.data.repository.SchedulesRepositoryImpl
import com.template.app.core.data.repository.SettingsRepositoryImpl
import com.template.app.core.data.repository.UserRepositoryImpl
import com.template.app.domain.repository.AudioRepository
import com.template.app.domain.repository.ClipboardRepository
import com.template.app.domain.repository.ConfigRepository
import com.template.app.domain.repository.DisplayRepository
import com.template.app.domain.repository.FilesystemRepository
import com.template.app.domain.repository.HealthRepository
import com.template.app.domain.repository.MaintenanceRepository
import com.template.app.domain.repository.MediaRepository
import com.template.app.domain.repository.MonitorRepository
import com.template.app.domain.repository.NetworkRepository
import com.template.app.domain.repository.NotificationsRepository
import com.template.app.domain.repository.PowerRepository
import com.template.app.domain.repository.ProcessesRepository
import com.template.app.domain.repository.SchedulesRepository
import com.template.app.domain.repository.SettingsRepository
import com.template.app.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Provides
    @Singleton
    fun provideDisplayRepository(impl: DisplayRepositoryImpl): DisplayRepository = impl

    @Provides
    @Singleton
    fun provideAudioRepository(impl: AudioRepositoryImpl): AudioRepository = impl

    @Provides
    @Singleton
    fun provideClipboardRepository(impl: ClipboardRepositoryImpl): ClipboardRepository = impl

    @Provides
    @Singleton
    fun provideFilesystemRepository(impl: FilesystemRepositoryImpl): FilesystemRepository = impl

    @Provides
    @Singleton
    fun provideNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository = impl

    @Provides
    @Singleton
    fun provideNetworkRepository(impl: NetworkRepositoryImpl): NetworkRepository = impl

    @Provides
    @Singleton
    fun provideProcessesRepository(impl: ProcessesRepositoryImpl): ProcessesRepository = impl

    @Provides
    @Singleton
    fun provideMonitorRepository(impl: MonitorRepositoryImpl): MonitorRepository = impl

    @Provides
    @Singleton
    fun providePowerRepository(impl: PowerRepositoryImpl): PowerRepository = impl

    @Provides
    @Singleton
    fun provideSchedulesRepository(impl: SchedulesRepositoryImpl): SchedulesRepository = impl

    @Provides
    @Singleton
    fun provideMediaRepository(impl: MediaRepositoryImpl): MediaRepository = impl

    @Provides
    @Singleton
    fun provideHealthRepository(impl: HealthRepositoryImpl): HealthRepository = impl

    @Provides
    @Singleton
    fun provideConfigRepository(impl: ConfigRepositoryImpl): ConfigRepository = impl

    @Provides
    @Singleton
    fun provideMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository = impl


}
