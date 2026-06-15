package com.template.app.core.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.template.app.BuildConfig
import com.template.app.core.data.remote.api.UserApiService
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.network.AuthInterceptor
import com.template.app.core.network.VelaInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VelaRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_LOGGING)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }

    // ── User API Client ──

    @Provides
    @Singleton
    @UserRetrofit
    fun provideUserOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @UserRetrofit
    fun provideUserRetrofit(
        @UserRetrofit okHttpClient: OkHttpClient, 
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideUserApiService(@UserRetrofit retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    // ── Vela Agent Client ──

    @Provides
    @Singleton
    @VelaRetrofit
    fun provideVelaOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        velaInterceptor: VelaInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(velaInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @VelaRetrofit
    fun provideVelaRetrofit(
        @VelaRetrofit okHttpClient: OkHttpClient, 
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/") // Placeholder, VelaInterceptor handles dynamic URL
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideVelaApiService(@VelaRetrofit retrofit: Retrofit): VelaApiService =
        retrofit.create(VelaApiService::class.java)
}
