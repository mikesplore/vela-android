package com.template.app.core.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PairingRequest(
    @Json(name = "pairing_code") val pairingCode: String,
    @Json(name = "pairing_pin") val pairingPin: String,
    @Json(name = "agent_label") val agentLabel: String = "Android Device"
)

@JsonClass(generateAdapter = true)
data class PairingResponse(
    val status: String,
    @Json(name = "agent_id") val agentId: String,
    @Json(name = "relay_base_url") val relayBaseUrl: String,
    val idempotent: Boolean,
    @Json(name = "relay_secret") val relaySecret: String,
    @Json(name = "relay_secret_shared") val relaySecretShared: Boolean
)

@JsonClass(generateAdapter = true)
data class RegistrationStatusResponse(
    @Json(name = "api_version") val apiVersion: String,
    val status: String,
    @Json(name = "relay_ready") val relayReady: Boolean
)
