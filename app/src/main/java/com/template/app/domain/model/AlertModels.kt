package com.template.app.domain.model

data class AlertDelivery(
    val id: Long,
    val createdAt: String,
    val alertKind: String,
    val channel: String,
    val status: String,
    val title: String,
    val body: String,
    val emailTo: String?,
    val emailProviderId: String?,
    val pushDelivered: Int?,
    val alertType: String?,
    val value: Double?,
    val threshold: Double?,
    val resource: String?,
)

data class AlertHistory(
    val alerts: List<AlertDelivery>,
    val totalStored: Int?,
    val todayCount: Int?
)
