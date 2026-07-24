package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.DockerComposeStatus
import com.template.app.domain.model.DockerContainer
import com.template.app.domain.model.DockerContainerDetail
import com.template.app.domain.model.DockerInfo
import com.template.app.domain.model.DockerLogs
import kotlinx.coroutines.flow.Flow

interface DockerRepository {
    fun observeInfo(): Flow<DockerInfo?>
    fun observeContainers(): Flow<List<DockerContainer>>

    suspend fun refreshInfo(): Resource<DockerInfo>
    suspend fun refreshContainers(all: Boolean = true, filter: String? = null): Resource<List<DockerContainer>>
    suspend fun getContainer(nameOrId: String): Resource<DockerContainerDetail>
    suspend fun getLogs(nameOrId: String, lines: Int = 100): Resource<DockerLogs>
    suspend fun start(nameOrId: String): Resource<String>
    suspend fun stop(nameOrId: String): Resource<String>
    suspend fun restart(nameOrId: String): Resource<String>
    suspend fun getCompose(
        projectDirectory: String? = null,
        project: String? = null
    ): Resource<DockerComposeStatus>
}
