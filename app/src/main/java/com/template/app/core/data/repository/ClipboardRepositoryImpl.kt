package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaClipboardEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ClipboardWriteRequest
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scopedNullable
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaClipboard
import com.template.app.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClipboardRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : ClipboardRepository {

    override fun observeClipboard(): Flow<VelaClipboard?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeClipboard(id).map { it?.let { VelaClipboard(it.content) } }
        }

    override suspend fun readClipboard(): Resource<String> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val data = apiService.readClipboard().data ?: "No data fetched"
        velaDao.upsertClipboard(VelaClipboardEntity.fromContent(connectionId, data))
        data
    }

    override suspend fun writeClipboard(text: String): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        apiService.writeClipboard(ClipboardWriteRequest(text))
        velaDao.upsertClipboard(VelaClipboardEntity.fromContent(connectionId, text))
        Unit
    }

    override suspend fun clearClipboard(): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        apiService.clearClipboard()
        velaDao.clearClipboard(connectionId)
        Unit
    }

}
