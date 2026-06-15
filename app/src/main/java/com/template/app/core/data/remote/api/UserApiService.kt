package com.template.app.core.data.remote.api

import com.template.app.core.data.remote.dto.UserDto
import retrofit2.http.*

interface UserApiService {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): UserDto

    @POST("users")
    suspend fun createUser(@Body body: Map<String, String>): UserDto

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): UserDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String)
}
