package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaHealthEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaHealth
import com.template.app.domain.repository.HealthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class HealthRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : HealthRepository {

    private val remoteHealth = MutableStateFlow<VelaHealth?>(null)

    override fun observeHealth(): Flow<VelaHealth?> =
        velaDao.observeHealth()
            .map { it?.toDomain() }
            .combine(remoteHealth) { local, remote ->
                // Prefer remote data if we have a fresh successful ping
                remote ?: local
            }
            .distinctUntilChanged()


    override suspend fun getHealth(): Resource<VelaHealth> = safeApiCall {
        try {
            val response = apiService.health()
            val domain = VelaHealth(
                status = response.status ?: "unknown",
                uptimeSeconds = response.uptimeSeconds ?: 0L
            )
            // Update headstart cache
            remoteHealth.value = domain
            // Update offline-first storage
            velaDao.upsertHealth(VelaHealthEntity.fromDomain(domain))
            domain
        } catch (e: Exception) {
            // If network fails, clear both headstart and cache so UI shows "Disconnected"
            remoteHealth.value = null
            velaDao.clearHealth()
            throw e
        }
    }
}