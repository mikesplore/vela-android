package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface FilesystemRepository {
    fun observeDisks(): Flow<List<VelaDiskUsage>>

    fun observeFiles(path: String): Flow<List<VelaFileInfo>>

    suspend fun listFiles(path: String?, showHidden: Boolean = false): Resource<VelaFileList>
    suspend fun getFileTree(path: String, maxDepth: Int = 1, showHidden: Boolean = false): Resource<VelaFileTree>
    suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>>
    suspend fun downloadFile(path: String, destination: File): Resource<File>
    suspend fun uploadFile(path: String, file: File): Resource<Unit>
    suspend fun deleteFile(path: String): Resource<Unit>
    suspend fun makeDirectory(path: String): Resource<Unit>
    suspend fun renameFile(from: String, to: String): Resource<Unit>
    suspend fun searchFiles(query: String, path: String?): Resource<List<VelaFileInfo>>
    suspend fun zipFiles(paths: List<String>, output: String): Resource<Unit>
    suspend fun unzipFile(path: String, destination: String): Resource<Unit>
    suspend fun openFile(path: String): Resource<Unit>
}
