package com.template.app.core.di

import com.template.app.core.data.repository.SettingsRepositoryImpl
import com.template.app.core.data.repository.UserRepositoryImpl
import com.template.app.core.data.repository.VelaRepositoryImpl
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
    fun provideVelaRepository(impl: VelaRepositoryImpl): VelaRepository = impl
}
