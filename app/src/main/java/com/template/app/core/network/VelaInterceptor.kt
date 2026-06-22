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
        val originalUrl = originalRequest.url

        if (settings.baseUrl.isBlank()) {
            return chain.proceed(originalRequest)
        }

        val settingsUrl = settings.baseUrl.let { 
            if (it.startsWith("http")) it else "http://$it"
        }.toHttpUrlOrNull() ?: return chain.proceed(originalRequest)

        // 1. Start with the Base URL from settings
        val newUrlBuilder = settingsUrl.newBuilder()
        
        // 2. Append original path segments (e.g., fs/list)
        val originalSegments = originalUrl.pathSegments
        for (segment in originalSegments) {
            if (segment.isNotBlank()) {
                newUrlBuilder.addPathSegment(segment)
            }
        }
        
        // 3. CRITICAL: Preserve ALL query parameters (like ?path=/home/mike)
        // Without this, the server defaults to a fallback directory.
        newUrlBuilder.encodedQuery(originalUrl.encodedQuery)

        val newRequest = originalRequest.newBuilder()
            .url(newUrlBuilder.build())
            .header("X-Secret", settings.apiToken)
            .build()

        return chain.proceed(newRequest)
    }
}
