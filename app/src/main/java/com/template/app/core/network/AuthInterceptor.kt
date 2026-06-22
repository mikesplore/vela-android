package com.template.app.core.network

import com.template.app.core.data.local.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Automatically attaches the Bearer token to every outgoing request.
 * Token is read from DataStore via [UserPreferencesDataStore].
 */
class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferencesDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { userPreferences.authToken.first() }

        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
