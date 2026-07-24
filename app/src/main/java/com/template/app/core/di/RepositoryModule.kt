package com.template.app.core.di

import com.template.app.core.data.repository.*
import com.template.app.domain.repository.*
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
    fun provideDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository = impl

    @Provides
    @Singleton
    fun providePairingRepository(impl: PairingRepositoryImpl): PairingRepository = impl

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

    @Provides
    @Singleton
    fun provideAssistantRepository(impl: AssistantRepositoryImpl): AssistantRepository = impl

    @Provides
    @Singleton
    fun provideCapabilitiesRepository(impl: CapabilitiesRepositoryImpl): CapabilitiesRepository = impl

    @Provides
    @Singleton
    fun provideDockerRepository(impl: DockerRepositoryImpl): DockerRepository = impl

    @Provides
    @Singleton
    fun providePushRepository(impl: PushRepositoryImpl): PushRepository = impl
}
