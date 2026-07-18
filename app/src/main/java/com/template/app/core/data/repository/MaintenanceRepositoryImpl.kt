package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaServiceEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaLogs
import com.template.app.domain.model.VelaMaintenanceUpdate
import com.template.app.domain.model.VelaPackageUpdate
import com.template.app.domain.model.VelaService
import com.template.app.domain.repository.MaintenanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : MaintenanceRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeServices(query: String, limit: Int): Flow<List<VelaService>> =
        activeConnection.connectionId.flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                velaDao.observeServices(id, query.trim(), limit).map { list ->
                    list.map { it.toDomain() }
                }
            }
        }

    override fun observeServiceCount(): Flow<Int> =
        activeConnection.scoped(0) { id ->
            velaDao.observeServiceCount(id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMatchedServiceCount(query: String): Flow<Int> =
        activeConnection.connectionId.flatMapLatest { id ->
            if (id == null) {
                flowOf(0)
            } else {
                velaDao.observeMatchedServiceCount(id, query.trim())
            }
        }

    override suspend fun clearCache(): Resource<Unit> = safeApiCall {
        apiService.clearCache()
        Unit
    }

    override suspend fun getLogs(service: String, lines: Int): Resource<VelaLogs> = safeApiCall {
        val res = apiService.getLogs(service, lines)
        VelaLogs(service = res.service ?: service, lines = res.lines ?: emptyList())
    }

    override suspend fun checkUpdates(): Resource<VelaMaintenanceUpdate> = safeApiCall {
        val res = apiService.checkUpdates()
        VelaMaintenanceUpdate(
            manager = res.manager.orEmpty(),
            packages = res.updates?.map {
                VelaPackageUpdate(
                    packageName = it.packageName.orEmpty(),
                    current = it.current,
                    available = it.available
                )
            } ?: emptyList()
        )
    }

    override suspend fun runUpdates(): Resource<Unit> = safeApiCall {
        apiService.runUpdates(confirm = true)
        Unit
    }

    override suspend fun syncTime(): Resource<Unit> = safeApiCall {
        apiService.syncTime()
        Unit
    }

    override suspend fun getServices(): Resource<List<VelaService>> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.getServices()
        val domains = res.services
            ?.mapNotNull {
                val name = it.name?.trim()?.takeIf { n -> n.isNotBlank() } ?: return@mapNotNull null
                VelaService(
                    name = name,
                    load = it.load.orEmpty(),
                    active = it.active.orEmpty(),
                    sub = it.sub.orEmpty(),
                    description = it.description.orEmpty()
                )
            }
            ?.distinctBy { it.name }
            .orEmpty()

        if (domains.isNotEmpty()) {
            velaDao.replaceServices(
                connectionId,
                domains.map { VelaServiceEntity.fromDomain(connectionId, it) }
            )
        }
        domains
    }

    override suspend fun startService(name: String): Resource<Unit> = safeApiCall {
        apiService.startService(name)
        Unit
    }

    override suspend fun stopService(name: String): Resource<Unit> = safeApiCall {
        apiService.stopService(name)
        Unit
    }

    override suspend fun restartService(name: String): Resource<Unit> = safeApiCall {
        apiService.restartService(name)
        Unit
    }
}
