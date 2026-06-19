package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaNotification
import kotlinx.coroutines.flow.Flow

interface NotificationsRepository {
    fun observeNotifications(): Flow<List<VelaNotification>>
    suspend fun getNotifications(): Resource<List<VelaNotification>>
}