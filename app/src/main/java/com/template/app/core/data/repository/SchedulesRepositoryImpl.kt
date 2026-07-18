package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaScheduledTaskEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.ScheduledTask
import com.template.app.core.data.remote.dto.SchedulerCreateRequest
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaScheduledTask
import com.template.app.domain.repository.SchedulesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
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
        fetchAndCacheTasks(connectionId) ?: emptyList()
    }

    override suspend fun createScheduledTask(
        command: String,
        args: List<String>,
        runAt: String,
        recurring: String?
    ): Resource<Unit> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val response = apiService.createScheduledTask(
            SchedulerCreateRequest(
                command = command,
                args = args,
                runAt = runAt,
                recurring = recurring
            )
        )

        val displayCommand = buildList {
            add(command)
            addAll(args)
        }.joinToString(" ")

        val jobId = parseJobId(response.message) ?: UUID.randomUUID().toString().take(8)
        val optimistic = VelaScheduledTask(
            id = jobId,
            command = displayCommand,
            nextRun = runAt,
            recurring = recurring
        )
        velaDao.upsertScheduledTasks(
            listOf(VelaScheduledTaskEntity.fromDomain(connectionId, optimistic))
        )

        val listed = fetchAndCacheTasks(connectionId)
        when {
            listed == null -> Unit // missing jobs/tasks field — keep optimistic row
            listed.isEmpty() -> {
                // Explicit empty list can race right after create; restore optimistic.
                velaDao.upsertScheduledTasks(
                    listOf(VelaScheduledTaskEntity.fromDomain(connectionId, optimistic))
                )
            }

            listed.none { it.id == jobId } -> {
                // Server list lagged; keep both remote rows + optimistic.
                velaDao.upsertScheduledTasks(
                    listOf(VelaScheduledTaskEntity.fromDomain(connectionId, optimistic))
                )
            }
        }
        Unit
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

    /**
     * @return mapped jobs, or null when the response has no usable `jobs`/`tasks` array
     * (so callers can avoid wiping local cache).
     */
    private suspend fun fetchAndCacheTasks(connectionId: Long): List<VelaScheduledTask>? {
        val response = apiService.listScheduledTasks()
        val rawJobs = response.jobs ?: response.tasks ?: return null
        val domains = rawJobs.mapNotNull { it.toDomainOrNull() }

        velaDao.replaceScheduledTasks(
            connectionId,
            domains.map { VelaScheduledTaskEntity.fromDomain(connectionId, it) }
        )
        return domains
    }

    private fun ScheduledTask.toDomainOrNull(): VelaScheduledTask? {
        val id = id?.takeIf { it.isNotBlank() } ?: return null
        val displayCommand = buildList {
            command?.takeIf { it.isNotBlank() }?.let { add(it) }
            args?.filter { it.isNotBlank() }?.let { addAll(it) }
        }.joinToString(" ").ifBlank { command.orEmpty() }
        if (displayCommand.isBlank()) return null

        val recurringLabel = recurring
            ?: trigger?.takeIf { !it.startsWith("date[", ignoreCase = true) }

        return VelaScheduledTask(
            id = id,
            command = displayCommand,
            nextRun = nextRun ?: runAt ?: "Unknown",
            recurring = recurringLabel,
            trigger = trigger
        )
    }

    private fun parseJobId(message: String?): String? {
        if (message.isNullOrBlank()) return null
        return JOB_ID_REGEX.find(message)?.groupValues?.getOrNull(1)
    }

    companion object {
        private val JOB_ID_REGEX = Regex("""Scheduled job\s+(\S+)""", RegexOption.IGNORE_CASE)
    }
}
