package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaDeviceEntity
import com.template.app.core.data.local.entities.VelaHealthEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaDevice
import com.template.app.domain.model.VelaHealth
import com.template.app.domain.repository.DeviceRepository
import com.template.app.domain.repository.HealthRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class HealthRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
    private val deviceRepository: DeviceRepository,
) : HealthRepository {

    private val remoteHealth = MutableStateFlow<VelaHealth?>(null)
    private val remoteDevice = MutableStateFlow<VelaDevice?>(null)

    override fun clearInMemoryCaches() {
        remoteHealth.value = null
        remoteDevice.value = null
    }

    override fun observeHealth(): Flow<VelaHealth?> =
        activeConnection.connectionId.flatMapLatest { id ->
            remoteHealth.value = null
            if (id == null) flowOf(null)
            else velaDao.observeHealth(id).map { it?.toDomain() }
                .combine(remoteHealth) { local, remote -> remote ?: local }
                .distinctUntilChanged()
        }

    override suspend fun getHealth(): Resource<VelaHealth> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        try {
            val response = apiService.health()
            val domain = VelaHealth(
                status = response.status ?: "unknown",
                uptimeSeconds = response.uptimeSeconds ?: 0L
            )
            remoteHealth.value = domain
            velaDao.upsertHealth(VelaHealthEntity.fromDomain(connectionId, domain))
            domain
        } catch (e: Exception) {
            remoteHealth.value = null
            velaDao.clearHealth(connectionId)
            throw e
        }
    }

    override fun observeDevice(): Flow<VelaDevice?> =
        activeConnection.connectionId.flatMapLatest { id ->
            remoteDevice.value = null
            if (id == null) flowOf(null)
            else velaDao.observeDevice(id).map { it?.toDomain() }
                .combine(remoteDevice) { local, remote -> remote ?: local }
                .distinctUntilChanged()
        }

    override suspend fun getDevice(): Resource<VelaDevice> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val response = apiService.getDevice()
        val domain = VelaDevice(
            laptopModel = response.laptopModel,
            hardwareVendor = response.hardwareVendor,
            osDistro = response.osDistro,
            osDistroVersion = response.osDistroVersion,
            kernel = response.kernel,
            architecture = response.architecture,
            hostname = response.hostname,
            prettyHostname = response.prettyHostname
        )
        remoteDevice.value = domain
        velaDao.upsertDevice(VelaDeviceEntity.fromDomain(connectionId, domain))
        runCatching {
            deviceRepository.updateDeviceMetadata(
                id = connectionId,
                hostname = domain.prettyHostname ?: domain.hostname,
                username = null
            )
        }
        domain
    }
}
