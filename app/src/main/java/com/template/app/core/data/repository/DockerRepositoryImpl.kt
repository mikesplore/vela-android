package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaDockerContainerEntity
import com.template.app.core.data.local.entities.VelaDockerInfoEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scoped
import com.template.app.core.device.scopedNullable
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.DockerComposeService
import com.template.app.domain.model.DockerComposeStatus
import com.template.app.domain.model.DockerContainer
import com.template.app.domain.model.DockerContainerDetail
import com.template.app.domain.model.DockerInfo
import com.template.app.domain.model.DockerLogs
import com.template.app.domain.repository.DockerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DockerRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider
) : DockerRepository {

    override fun observeInfo(): Flow<DockerInfo?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeDockerInfo(id).map { it?.toDomain() }
        }

    override fun observeContainers(): Flow<List<DockerContainer>> =
        activeConnection.scoped(emptyList()) { id ->
            velaDao.observeDockerContainers(id).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun refreshInfo(): Resource<DockerInfo> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val dto = apiService.getDockerInfo()
        val info = DockerInfo(
            installed = dto.installed,
            running = dto.running,
            version = dto.version,
            containersRunning = dto.containersRunning,
            containersTotal = dto.containersTotal,
            message = dto.message
        )
        velaDao.upsertDockerInfo(VelaDockerInfoEntity.fromDomain(connectionId, info))
        info
    }

    override suspend fun refreshContainers(
        all: Boolean,
        filter: String?
    ): Resource<List<DockerContainer>> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        val containers = apiService.getDockerContainers(all = all, filter = filter)
            .containers
            .mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                DockerContainer(
                    id = id,
                    name = dto.name.orEmpty(),
                    image = dto.image.orEmpty(),
                    status = dto.status.orEmpty(),
                    state = dto.state.orEmpty(),
                    ports = dto.ports,
                    created = dto.created
                )
            }
        velaDao.replaceDockerContainers(
            connectionId,
            containers.map { VelaDockerContainerEntity.fromDomain(connectionId, it) }
        )
        containers
    }

    override suspend fun getContainer(nameOrId: String): Resource<DockerContainerDetail> =
        safeApiCall {
            val dto = apiService.getDockerContainer(nameOrId)
            DockerContainerDetail(
                id = dto.id.orEmpty(),
                name = dto.name.orEmpty(),
                image = dto.image.orEmpty(),
                status = dto.status.orEmpty(),
                state = dto.state.orEmpty(),
                health = dto.health,
                ports = dto.ports.orEmpty(),
                startedAt = dto.startedAt,
                finishedAt = dto.finishedAt
            )
        }

    override suspend fun getLogs(nameOrId: String, lines: Int): Resource<DockerLogs> =
        safeApiCall {
            val dto = apiService.getDockerContainerLogs(nameOrId, lines)
            DockerLogs(
                container = dto.container ?: nameOrId,
                lines = dto.lines
            )
        }

    override suspend fun start(nameOrId: String): Resource<String> = safeApiCall {
        val res = apiService.startDockerContainer(nameOrId)
        if (res.success == false) error(res.message ?: "Failed to start container")
        res.message ?: "Container started"
    }

    override suspend fun stop(nameOrId: String): Resource<String> = safeApiCall {
        val res = apiService.stopDockerContainer(nameOrId)
        if (res.success == false) error(res.message ?: "Failed to stop container")
        res.message ?: "Container stopped"
    }

    override suspend fun restart(nameOrId: String): Resource<String> = safeApiCall {
        val res = apiService.restartDockerContainer(nameOrId)
        if (res.success == false) error(res.message ?: "Failed to restart container")
        res.message ?: "Container restarted"
    }

    override suspend fun getCompose(
        projectDirectory: String?,
        project: String?
    ): Resource<DockerComposeStatus> = safeApiCall {
        val dto = apiService.getDockerCompose(projectDirectory, project)
        DockerComposeStatus(
            project = dto.project,
            services = dto.services.map {
                DockerComposeService(
                    name = it.name.orEmpty(),
                    state = it.state.orEmpty(),
                    status = it.status.orEmpty(),
                    ports = it.ports
                )
            }
        )
    }
}
