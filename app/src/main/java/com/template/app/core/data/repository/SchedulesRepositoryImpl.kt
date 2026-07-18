package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaScheduledTaskEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.SchedulerCreateRequest
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaScheduledTask
import com.template.app.domain.repository.SchedulesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SchedulesRepositoryImpl @Inject constructor(

    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : SchedulesRepository {

    override fun observeScheduledTasks(): Flow<List<VelaScheduledTask>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeScheduledTasks(id).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun getScheduledTasks(): Resource<List<VelaScheduledTask>> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val response = apiService.listScheduledTasks()
        val domains = response.jobs?.map {
            VelaScheduledTask(id = it.id ?: "", command = it.command ?: "", nextRun = it.nextRun ?: it.runAt ?: "Unknown", recurring = it.recurring)
        } ?: emptyList()
        velaDao.replaceScheduledTasks(connectionId, domains.map { VelaScheduledTaskEntity.fromDomain(connectionId, it) })
        domains
    }

    override suspend fun createScheduledTask(command: String, runAt: String, recurring: String?): Resource<VelaScheduledTask> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val res = apiService.createScheduledTask(SchedulerCreateRequest(command, runAt, recurring))
        val domain = VelaScheduledTask(id = res.id ?: "", command = res.command ?: command, nextRun = res.nextRun ?: runAt, recurring = res.recurring ?: recurring)
        velaDao.upsertScheduledTasks(listOf(VelaScheduledTaskEntity.fromDomain(connectionId, domain)))
        domain
    }

    override suspend fun cancelScheduledTask(taskId: String): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        apiService.cancelScheduledTask(taskId)
        velaDao.deleteScheduledTask(connectionId, taskId)
        Unit
    }

    override suspend fun runTaskNow(taskId: String): Resource<Unit> = safeApiCall {
        apiService.runTaskNow(taskId)
        Unit
    }

}
