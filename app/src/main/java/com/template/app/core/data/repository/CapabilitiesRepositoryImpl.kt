package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaAssistantToolEntity
import com.template.app.core.data.local.entities.VelaCapabilitiesMetaEntity
import com.template.app.core.data.local.entities.VelaCapabilityModuleEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.device.scopedNullable
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.HostCapabilities
import com.template.app.domain.repository.CapabilitiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilitiesRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider
) : CapabilitiesRepository {

    override fun observeCapabilities(): Flow<HostCapabilities?> =
        activeConnection.scopedNullable { id ->
            combine(
                velaDao.observeCapabilitiesMeta(id),
                velaDao.observeCapabilityModules(id),
                velaDao.observeAvailableAssistantTools(id)
            ) { meta, modules, tools ->
                if (meta == null) null
                else HostCapabilities(
                    checkedAt = meta.checkedAt,
                    modules = modules.associate { it.moduleKey to it.toDomain() },
                    availableAssistantTools = tools.map { it.toolName },
                    fetchedAtMillis = meta.fetchedAtMillis
                )
            }
        }

    override fun observeAvailableAssistantTools(): Flow<List<String>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeAvailableAssistantTools(id).map { list -> list.map { it.toolName } }
        }

    override suspend fun fetchCapabilities(refreshProbes: Boolean): Resource<HostCapabilities> =
        safeApiCall {
            val connectionId = activeConnection.requireActiveId()
            val response = apiService.getCapabilities(refresh = refreshProbes)
            persist(connectionId, response.checkedAt, response)
        }

    override suspend fun refreshAndFetch(): Resource<HostCapabilities> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        apiService.refreshCapabilities()
        val response = apiService.getCapabilities(refresh = false)
        persist(connectionId, response.checkedAt, response)
    }

    override suspend fun hasCachedCapabilities(): Boolean {
        val id = activeConnection.connectionId.value ?: return false
        return velaDao.getCapabilitiesMeta(id)?.fetchedAtMillis?.let { it > 0L } == true
    }

    private suspend fun persist(
        connectionId: Long,
        checkedAt: String?,
        response: com.template.app.core.data.remote.dto.CapabilitiesResponse
    ): HostCapabilities {
        val modules = response.modules.map { (key, dto) ->
            VelaCapabilityModuleEntity(
                connectionId = connectionId,
                moduleKey = key,
                available = dto.available,
                configEnabled = dto.configEnabled,
                reason = dto.reason,
                missingCommands = dto.missingCommands.orEmpty()
            )
        }
        val availableTools = response.assistantTools?.available.orEmpty().map { name ->
            VelaAssistantToolEntity(
                connectionId = connectionId,
                toolName = name,
                available = true,
                unavailableReason = null
            )
        }
        // Persist available tools only (unavailable ones are not shown in UI)
        val meta = VelaCapabilitiesMetaEntity(
            connectionId = connectionId,
            checkedAt = checkedAt,
            fetchedAtMillis = System.currentTimeMillis()
        )
        velaDao.replaceCapabilities(connectionId, meta, modules, availableTools)
        return HostCapabilities(
            checkedAt = checkedAt,
            modules = modules.associate { it.moduleKey to it.toDomain() },
            availableAssistantTools = availableTools.map { it.toolName },
            fetchedAtMillis = meta.fetchedAtMillis
        )
    }
}
