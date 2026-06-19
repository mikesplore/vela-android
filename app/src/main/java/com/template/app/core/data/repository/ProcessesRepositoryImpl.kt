package com.template.app.core.data.repository

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaActiveWindowEntity
import com.template.app.core.data.local.entities.VelaProcessEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ProcessItem
import com.template.app.core.data.remote.dto.ProcessesResponse
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaProcess
import com.template.app.domain.repository.ProcessesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProcessesRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val moshi: Moshi
) : ProcessesRepository {

    override fun observeActiveWindow(): Flow<String?> =
        velaDao.observeActiveWindow().map { it?.title }

    override fun observeProcesses(limit: Int): Flow<List<VelaProcess>> =
        velaDao.observeProcesses(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getProcesses(): Resource<List<VelaProcess>> = safeApiCall {
        val jsonStr = apiService.getProcesses().string()
        val domains = parseProcessesResiliently(jsonStr)
        velaDao.replaceProcesses(domains.map { VelaProcessEntity.fromDomain(it) })
        domains
    }

    override suspend fun getActiveWindow(): Resource<String> = safeApiCall {
        val title = apiService.getActiveWindow().title ?: ""
        velaDao.upsertActiveWindow(VelaActiveWindowEntity.fromTitle(title))
        title
    }

    override suspend fun killProcess(pid: Int): Resource<Unit> = safeApiCall {
        apiService.killProcessByPid(pid)
        Unit
    }

    private fun parseProcessesResiliently(jsonStr: String): List<VelaProcess> {
        try {
            val listType = Types.newParameterizedType(List::class.java, ProcessItem::class.java)
            val adapter = moshi.adapter<List<ProcessItem>>(listType)
            val list = adapter.fromJson(jsonStr)
            if (list != null) return list.map { it.toDomain() }
        } catch (e: Exception) {
        }

        try {
            val adapter = moshi.adapter(ProcessesResponse::class.java)
            val obj = adapter.fromJson(jsonStr)
            if (obj?.topByCpu != null) return obj.topByCpu.map { it.toDomain() }
        } catch (e: Exception) {
        }

        try {
            val mapType =
                Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)
            val parsedMap = adapter.fromJson(jsonStr)
            if (parsedMap != null) {
                for ((_, value) in parsedMap) {
                    if (value is List<*>) {
                        val subJson = moshi.adapter(Any::class.java).toJson(value)
                        val listType =
                            Types.newParameterizedType(List::class.java, ProcessItem::class.java)
                        val list = moshi.adapter<List<ProcessItem>>(listType).fromJson(subJson)
                        if (list != null) return list.map { it.toDomain() }
                    }
                }
            }
        } catch (e: Exception) {
        }

        return emptyList()
    }

    private fun ProcessItem.toDomain() = VelaProcess(
        pid = pid ?: 0,
        name = name ?: "Unknown",
        cpu = cpu ?: 0.0,
        mem = mem ?: 0.0,
        username = username,
        memoryRss = memRss
    )

}