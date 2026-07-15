package com.template.app.core.data.remote.api

import com.template.app.core.data.remote.dto.PairingRequest
import com.template.app.core.data.remote.dto.PairingResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface PairingApiService {
    @POST
    suspend fun completePairing(
        @Url url: String,
        @Body request: PairingRequest
    ): PairingResponse
}
