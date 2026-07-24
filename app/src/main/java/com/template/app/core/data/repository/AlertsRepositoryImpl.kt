package com.template.app.core.data.repository

import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.AlertDeliveryDto
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.AlertDelivery
import com.template.app.domain.model.AlertHistory
import com.template.app.domain.repository.AlertsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertsRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService
) : AlertsRepository {

    override suspend fun getHistory(
        limit: Int,
        offset: Int,
        alertKind: String?,
        channel: String?,
        sinceMinutes: Int?
    ): Resource<AlertHistory> = safeApiCall {
        val response = apiService.getAlertHistory(
            limit = limit,
            offset = offset,
            alertKind = alertKind,
            channel = channel,
            sinceMinutes = sinceMinutes
        )
        AlertHistory(
            alerts = response.alerts.map { it.toDomain() },
            totalStored = response.totalStored,
            todayCount = response.todayCount
        )
    }

    private fun AlertDeliveryDto.toDomain() = AlertDelivery(
        id = id ?: 0L,
        createdAt = createdAt.orEmpty(),
        alertKind = alertKind.orEmpty(),
        channel = channel.orEmpty(),
        status = status.orEmpty(),
        title = title.orEmpty(),
        body = body.orEmpty(),
        emailTo = emailTo,
        emailProviderId = emailProviderId,
        pushDelivered = pushDelivered,
        alertType = alertType,
        value = value,
        threshold = threshold,
        resource = resource
    )
}
