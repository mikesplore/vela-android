package com.template.app.core.device

import com.template.app.domain.model.PairedDevice
import com.template.app.domain.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveConnectionProvider @Inject constructor(
    deviceRepository: DeviceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val activeDevice: StateFlow<PairedDevice?> = deviceRepository.observeActiveDevice()
        .stateIn(scope, SharingStarted.Eagerly, null)

    val connectionId: StateFlow<Long?> = activeDevice
        .map { it?.id }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun current(): PairedDevice? = activeDevice.value

    fun requireActive(): PairedDevice =
        activeDevice.value
            ?: throw IllegalStateException("No active paired device")

    fun requireActiveId(): Long = requireActive().id
}
