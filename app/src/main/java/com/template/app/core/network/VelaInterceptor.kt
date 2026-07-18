package com.template.app.core.network

import com.template.app.domain.repository.DeviceRepository
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the active paired device's relay base URL and X-Secret to every request.
 */
class VelaInterceptor @Inject constructor(
    private val deviceRepository: DeviceRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val device = runBlocking { deviceRepository.getActiveDevice() }
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        if (device == null || device.relayBaseUrl.isBlank()) {
            return chain.proceed(originalRequest)
        }

        val settingsUrl = device.relayBaseUrl.let {
            if (it.startsWith("http")) it else "http://$it"
        }.toHttpUrlOrNull() ?: return chain.proceed(originalRequest)

        val newUrlBuilder = settingsUrl.newBuilder()

        for (segment in originalUrl.pathSegments) {
            if (segment.isNotBlank()) {
                newUrlBuilder.addPathSegment(segment)
            }
        }

        newUrlBuilder.encodedQuery(originalUrl.encodedQuery)

        val newRequest = originalRequest.newBuilder()
            .url(newUrlBuilder.build())
            .header("X-Secret", device.relaySecret)
            .build()

        return chain.proceed(newRequest)
    }
}
