package com.template.app.core.network

import com.squareup.moshi.Moshi
import com.template.app.core.data.remote.dto.ApiErrorResponse
import com.template.app.core.utils.AppEventManager
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject

/**
 * Intercepts network responses and emits snackbar events for errors.
 */
class ErrorInterceptor @Inject constructor(
    private val appEventManager: AppEventManager,
    private val moshi: Moshi
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            val errorMessage = "Network error — check your connection"
            appEventManager.showNetworkErrorSnackbar(errorMessage)
            appEventManager.addNetworkErrorLog(
                url = request.url.toString(),
                method = request.method,
                code = 0,
                message = errorMessage
            )
            throw e
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string()
            val apiError = errorBody?.let {
                try {
                    moshi.adapter(ApiErrorResponse::class.java).fromJson(it)
                } catch (e: Exception) {
                    null
                }
            }

            val errorMessage = apiError?.message ?: when (response.code) {
                401 -> "Unauthorized — please log in again"
                403 -> "Forbidden"
                404 -> "Resource not found"
                500 -> "Server error, please try again later"
                else -> "Error ${response.code}: ${response.message}"
            }
            appEventManager.showNetworkErrorSnackbar(errorMessage)
            
            appEventManager.addNetworkErrorLog(
                url = request.url.toString(),
                method = request.method,
                code = response.code,
                message = errorMessage
            )

            // Re-create the response body since we've consumed it
            return response.newBuilder()
                .body((errorBody ?: "").toResponseBody(response.body?.contentType()))
                .build()
        }

        return response
    }
}
