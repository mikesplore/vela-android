package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ClipboardRepository {
    fun observeClipboard(): Flow<VelaClipboard?>
    suspend fun readClipboard(): Resource<String>
    suspend fun writeClipboard(text: String): Resource<Unit>
    suspend fun clearClipboard(): Resource<Unit>

}
