package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaDiskEntity
import com.template.app.core.data.local.entities.VelaFileEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.*
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.*
import com.template.app.domain.repository.FilesystemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class FilesystemRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao
) : FilesystemRepository {

    override fun observeFiles(path: String): Flow<List<VelaFileInfo>> =
        velaDao.observeFiles(normalizePath(path)).map { list -> list.map { it.toDomain() } }

    override fun observeDisks(): Flow<List<VelaDiskUsage>> =
        velaDao.observeDisks().map { list -> list.map { it.toDomain() } }


    override suspend fun listFiles(path: String?, showHidden: Boolean): Resource<VelaFileList> = safeApiCall {
        val normalizedReqPath = normalizePath(path ?: "")
        val response = apiService.listFiles(normalizedReqPath, showHidden)
        val fileDomains = response.files?.map { it.toDomain() } ?: emptyList()

        val currentPath = normalizePath(response.currentPath ?: normalizedReqPath)

        val domain = VelaFileList(
            currentPath = currentPath,
            parentPath = response.parentPath?.let { normalizePath(it) },
            totalItems = response.totalItems ?: fileDomains.size,
            showHidden = response.showHidden ?: showHidden,
            files = fileDomains
        )

        velaDao.replaceFiles(currentPath, fileDomains.map { VelaFileEntity.fromDomain(it, currentPath) })
        domain
    }

    override suspend fun getFileTree(path: String, maxDepth: Int, showHidden: Boolean): Resource<VelaFileTree> = safeApiCall {
        val normalizedPath = normalizePath(path)
        val response = apiService.getTree(normalizedPath, maxDepth, showHidden)
        VelaFileTree(
            root = response.root?.toDomain() ?: VelaFileInfo("", normalizedPath, "directory", 0L, 0.0),
            children = response.children?.map { it.toDomain() } ?: emptyList(),
            breadcrumbs = response.breadcrumbs?.map { VelaBreadcrumb(it.name ?: "", normalizePath(it.path ?: "")) } ?: emptyList()
        )
    }

    override suspend fun getDiskUsage(): Resource<List<VelaDiskUsage>> = safeApiCall {
        val domains = apiService.getDiskUsage().usage?.map {
            VelaDiskUsage(
                mountpoint = it.mountpoint ?: "",
                total = it.total ?: "0",
                used = it.used?: "0",
                free = it.free ?: "0",
                percent = it.percent ?: 0.0
            )
        } ?: emptyList()
        velaDao.replaceDisks(domains.map { VelaDiskEntity.fromDomain(it) })
        domains
    }

    override suspend fun downloadFile(path: String, destination: File): Resource<File> = safeApiCall {
        val body = apiService.downloadFile(path)
        body.byteStream().use { inputStream ->
            destination.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        destination
    }

    override suspend fun uploadFile(path: String, file: File): Resource<Unit> = safeApiCall {
        val pathBody = path.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val multipart = MultipartBody.Part.createFormData("file", file.name, fileBody)
        apiService.uploadFile(pathBody, multipart)
        Unit
    }

    override suspend fun deleteFile(path: String): Resource<Unit> = safeApiCall {
        apiService.deleteFile(FilePathRequest(path))
        Unit
    }

    override suspend fun makeDirectory(path: String): Resource<Unit> = safeApiCall {
        apiService.makeDirectory(FilePathRequest(path))
        Unit
    }

    override suspend fun renameFile(from: String, to: String): Resource<Unit> = safeApiCall {
        apiService.renameFile(FileRenameRequest(from, to))
        Unit
    }

    override suspend fun searchFiles(query: String, path: String?): Resource<List<VelaFileInfo>> = safeApiCall {
        apiService.searchFiles(query, path).files?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun zipFiles(paths: List<String>, output: String): Resource<Unit> = safeApiCall {
        apiService.zipFiles(ZipRequest(paths, output))
        Unit
    }

    override suspend fun unzipFile(path: String, destination: String): Resource<Unit> = safeApiCall {
        apiService.unzipFile(UnzipRequest(path, destination))
        Unit
    }

    override suspend fun openFile(path: String): Resource<Unit> = safeApiCall {
        apiService.openFile(FilePathRequest(path))
        Unit
    }

    private fun FileItem.toDomain() = VelaFileInfo(
        name = name ?: "",
        path = normalizePath(path ?: ""),
        type = type ?: "file",
        size = size ?: 0L,
        modified = modified ?: 0.0,
        isHidden = isHidden ?: false,
        hasChildren = hasChildren ?: false,
        childrenCount = childrenCount,
        extension = extension
    )

    private fun normalizePath(path: String): String {
        if (path.isEmpty()) return ""
        if (path == "/") return "/"
        return path.removeSuffix("/")
    }
}
