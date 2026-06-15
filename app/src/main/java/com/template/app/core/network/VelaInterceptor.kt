package com.template.app.core.network

import com.template.app.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class VelaInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = runBlocking { settingsRepository.getSettings() }
        val originalRequest = chain.request()

        if (settings.baseUrl.isBlank()) {
            return chain.proceed(originalRequest)
        }

        val settingsUrl = settings.baseUrl.let { 
            if (it.startsWith("http")) it else "http://$it"
        }.toHttpUrlOrNull() ?: return chain.proceed(originalRequest)

        // 1. Start with the Base URL from settings (including paths like /relay/my-laptop)
        val newUrlBuilder = settingsUrl.newBuilder()
        
        // 2. Clear placeholder segments from the original request (usually just the endpoint name)
        // and append them to the settings base path.
        val originalSegments = originalRequest.url.pathSegments
        for (segment in originalSegments) {
            // "localhost" or empty segments from the placeholder are ignored
            if (segment.isNotBlank()) {
                newUrlBuilder.addPathSegment(segment)
            }
        }

        val newRequest = originalRequest.newBuilder()
            .url(newUrlBuilder.build())
            .header("X-API-Key", settings.apiToken)
            .build()

        return chain.proceed(newRequest)
    }
}
