package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaConfigEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scopedNullable
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaConfig
import com.template.app.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : ConfigRepository
{

    override fun observeConfig(): Flow<VelaConfig?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeConfig(id).map { it?.toDomain() }
        }

    override suspend fun getConfig(): Resource<VelaConfig> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val response = apiService.getConfig()
        val domain = VelaConfig(
            homeDirectory = response.homeDirectory,
            username = response.username
        )

        velaDao.upsertConfig(VelaConfigEntity.fromDomain(connectionId, domain))
        domain
    }

    override suspend fun setConfig(config: VelaConfig): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        velaDao.upsertConfig(VelaConfigEntity.fromDomain(connectionId, config))
        Unit
    }
}
