package com.template.app.domain.repository

import com.template.app.core.utils.Resource

interface PushRepository {
    suspend fun registerDevice(token: String, installationId: String?): Resource<Unit>
    suspend fun unregisterDevice(token: String): Resource<Unit>
}
