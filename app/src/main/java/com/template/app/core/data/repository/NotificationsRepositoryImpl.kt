package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaNotificationEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaNotification
import com.template.app.domain.repository.NotificationsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationsRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
) : NotificationsRepository
{
    override fun observeNotifications(): Flow<List<VelaNotification>> =
        velaDao.observeNotifications().map { list -> list.map { it.toDomain() } }

    override suspend fun getNotifications(): Resource<List<VelaNotification>> = safeApiCall {
        val domains = apiService.getNotifications().notifications?.map {
            VelaNotification(
                id = it.id?.toString() ?: "",
                title = it.title ?: "",
                message = it.message ?: "",
                appName = it.appName,
                timestamp = System.currentTimeMillis()
            )
        } ?: emptyList()
        velaDao.replaceNotifications(domains.map { VelaNotificationEntity.fromDomain(it) })
        domains
    }
}