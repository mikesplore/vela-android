package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaConfig
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun observeConfig(): Flow<VelaConfig?>
    suspend fun getConfig(): Resource<VelaConfig>
    suspend fun setConfig(config: VelaConfig): Resource<Unit>
}