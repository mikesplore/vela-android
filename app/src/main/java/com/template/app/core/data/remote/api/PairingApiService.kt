package com.template.app.core.data.remote.api

import com.template.app.core.data.remote.dto.PairingRequest
import com.template.app.core.data.remote.dto.PairingResponse
import com.template.app.core.data.remote.dto.RegistrationStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface PairingApiService {
    @POST
    suspend fun completePairing(
        @Url url: String,
        @Body request: PairingRequest
    ): PairingResponse

    @GET
    suspend fun getRegistrationStatus(
        @Url url: String,
        @Query("agent_id") agentId: String
    ): RegistrationStatusResponse
}
