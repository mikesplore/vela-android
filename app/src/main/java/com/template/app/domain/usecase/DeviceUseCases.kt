package com.template.app.domain.usecase

import com.template.app.core.utils.Resource
import com.template.app.domain.model.PairedDevice
import com.template.app.domain.model.VelaConfig
import com.template.app.domain.repository.ConfigRepository
import com.template.app.domain.repository.DeviceRepository
import com.template.app.domain.repository.PairingRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDevicesUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<List<PairedDevice>> = repository.observeDevices()
}

class ObserveActiveDeviceUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    operator fun invoke(): Flow<PairedDevice?> = repository.observeActiveDevice()
}

class SwitchDeviceUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(id: Long) = repository.switchDevice(id)
}

class RenameDeviceUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(id: Long, label: String) = repository.renameDevice(id, label)
}

class RemoveDeviceUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    /** @return true if any devices remain */
    suspend operator fun invoke(id: Long): Boolean {
        repository.removeDevice(id)
        return repository.hasDevices()
    }
}

class RemoveAllDevicesUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke() = repository.removeAllDevices()
}

class HasDevicesUseCase @Inject constructor(
    private val repository: DeviceRepository
) {
    suspend operator fun invoke(): Boolean = repository.hasDevices()
}

/**
 * Completes relay pairing, waits for the agent, persists the device, and returns config username.
 */
class PairDeviceUseCase @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val deviceRepository: DeviceRepository,
    private val configRepository: ConfigRepository
) {
    data class Result(
        val device: PairedDevice,
        val username: String?,
        val config: VelaConfig?
    )

    suspend operator fun invoke(
        pairUrl: String,
        pairingCode: String,
        pairingPin: String,
        vpsUrl: String? = null,
        labelHint: String? = null
    ): Resource<Result> {
        return when (val pairResult = pairingRepository.completePairing(pairUrl, pairingCode, pairingPin)) {
            is Resource.Success -> {
                val data = pairResult.data
                val statusUrl = when {
                    pairUrl.endsWith("/pair/complete") ->
                        pairUrl.replace("/pair/complete", "/agents/register/status")
                    pairUrl.contains("/pair/complete") ->
                        pairUrl.replace("/pair/complete", "/agents/register/status")
                    else -> pairUrl.substringBeforeLast("/") + "/status"
                }

                var isRelayReady = false
                for (i in 1..10) {
                    val statusResult = pairingRepository.getRegistrationStatus(statusUrl, data.agentId)
                    if (statusResult is Resource.Success && statusResult.data.relayReady) {
                        isRelayReady = true
                        break
                    }
                    delay(1500)
                }

                if (!isRelayReady) {
                    return Resource.Error("Relay is not ready yet. Please ensure your agent is running.")
                }

                // Persist device first so VelaInterceptor can auth config fetch
                val label = labelHint?.takeIf { it.isNotBlank() } ?: "Device"
                var device = deviceRepository.addOrUpdateDevice(
                    agentId = data.agentId,
                    relayBaseUrl = data.relayBaseUrl,
                    relaySecret = data.relaySecret,
                    label = label,
                    vpsUrl = vpsUrl
                )

                var config: VelaConfig? = null
                var username: String? = null
                when (val configResult = configRepository.getConfig()) {
                    is Resource.Success -> {
                        config = configResult.data
                        username = config.username
                        val betterLabel = when {
                            !labelHint.isNullOrBlank() && labelHint != "Device" -> labelHint
                            !username.isNullOrBlank() -> username
                            else -> device.label
                        }
                        device = deviceRepository.addOrUpdateDevice(
                            agentId = data.agentId,
                            relayBaseUrl = data.relayBaseUrl,
                            relaySecret = data.relaySecret,
                            label = betterLabel,
                            username = username,
                            vpsUrl = vpsUrl
                        )
                    }
                    is Resource.Error -> {
                        // Device is paired; config can load later
                    }
                    else -> {}
                }

                Resource.Success(Result(device = device, username = username, config = config))
            }
            is Resource.Error -> Resource.Error(pairResult.message)
            is Resource.Loading -> Resource.Loading
        }
    }
}
