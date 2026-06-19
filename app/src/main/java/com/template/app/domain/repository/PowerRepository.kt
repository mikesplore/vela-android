package com.template.app.domain.repository

import com.template.app.core.utils.Resource

interface PowerRepository {
    suspend fun shutdown(): Resource<Unit>
    suspend fun restart(): Resource<Unit>
    suspend fun sleep(): Resource<Unit>
    suspend fun hibernate(): Resource<Unit>
    suspend fun scheduleShutdown(at: String): Resource<Unit>
    suspend fun cancelShutdown(): Resource<Unit>
    suspend fun getPowerProfile(): Resource<String>
    suspend fun setPowerProfile(profile: String): Resource<Unit>
}
