package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.HostCapabilities
import kotlinx.coroutines.flow.Flow

interface CapabilitiesRepository {
    fun observeCapabilities(): Flow<HostCapabilities?>

    fun observeAvailableAssistantTools(): Flow<List<String>>

    /** Fetch and persist. Retries are the caller's responsibility when blocking UX. */
    suspend fun fetchCapabilities(refreshProbes: Boolean = false): Resource<HostCapabilities>

    /** Server-side re-probe, then fetch snapshot. */
    suspend fun refreshAndFetch(): Resource<HostCapabilities>

    suspend fun hasCachedCapabilities(): Boolean
}
