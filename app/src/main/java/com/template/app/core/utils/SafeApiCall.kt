package com.template.app.core.utils

import retrofit2.HttpException
import java.io.IOException

/**
 * Wraps a suspend Retrofit/Room call in a try-catch and maps the result
 * to a [Resource]. Use this in every Repository to avoid boilerplate.
 *
 * Example:
 *   override suspend fun getUsers(): Resource<List<User>> = safeApiCall {
 *       apiService.getUsers().map { it.toDomain() }
 *   }
 */
suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(call())
    } catch (e: HttpException) {
        val errorMessage = when (e.code()) {
            400 -> "Bad request"
            401 -> "Unauthorized — please log in again"
            403 -> "Forbidden"
            404 -> "Resource not found"
            408 -> "Request timed out"
            500 -> "Server error, please try again later"
            else -> "HTTP error ${e.code()}"
        }
        Resource.Error(errorMessage, e)
    } catch (e: IOException) {
        Resource.Error("Network error — check your connection", e)
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "An unexpected error occurred", e)
    }
}
