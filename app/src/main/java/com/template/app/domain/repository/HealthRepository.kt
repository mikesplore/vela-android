package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaHealth
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    suspend fun getHealth(): Resource<VelaHealth>

    fun observeHealth(): Flow<VelaHealth?>
}