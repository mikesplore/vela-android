package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaClipboardEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ClipboardWriteRequest
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaClipboard
import com.template.app.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClipboardRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : ClipboardRepository {

    override fun observeClipboard(): Flow<VelaClipboard?> =
        velaDao.observeClipboard().map { it?.let { VelaClipboard(it.content) } }

    override suspend fun readClipboard(): Resource<String> = safeApiCall {
        val data = apiService.readClipboard().data ?: ""
        velaDao.upsertClipboard(VelaClipboardEntity.fromContent(data))
        data
    }

    override suspend fun writeClipboard(text: String): Resource<Unit> = safeApiCall {
        apiService.writeClipboard(ClipboardWriteRequest(text))
        velaDao.upsertClipboard(VelaClipboardEntity.fromContent(text))
        Unit
    }

    override suspend fun clearClipboard(): Resource<Unit> = safeApiCall {
        apiService.clearClipboard()
        velaDao.clearClipboard()
        Unit
    }

}