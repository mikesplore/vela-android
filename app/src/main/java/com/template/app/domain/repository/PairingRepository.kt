package com.template.app.domain.repository

import com.template.app.core.data.remote.dto.PairingResponse
import com.template.app.core.data.remote.dto.RegistrationStatusResponse
import com.template.app.core.utils.Resource

interface PairingRepository {
    suspend fun completePairing(pairUrl: String, code: String, pin: String): Resource<PairingResponse>
    suspend fun getRegistrationStatus(url: String, agentId: String): Resource<RegistrationStatusResponse>
}
