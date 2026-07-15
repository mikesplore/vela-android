package com.template.app.core.data.repository

import com.template.app.core.data.remote.api.PairingApiService
import com.template.app.core.data.remote.dto.PairingRequest
import com.template.app.core.data.remote.dto.PairingResponse
import com.template.app.core.utils.Resource
import com.template.app.domain.repository.PairingRepository
import javax.inject.Inject

class PairingRepositoryImpl @Inject constructor(
    private val pairingApiService: PairingApiService
) : PairingRepository {

    override suspend fun completePairing(pairUrl: String, code: String, pin: String): Resource<PairingResponse> {
        return try {
            val response = pairingApiService.completePairing(pairUrl, PairingRequest(code, pin))
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}
