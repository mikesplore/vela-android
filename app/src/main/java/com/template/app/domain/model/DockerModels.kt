package com.template.app.domain.model

data class DockerInfo(
    val installed: Boolean,
    val running: Boolean,
    val version: String? = null,
    val containersRunning: Int? = null,
    val containersTotal: Int? = null,
    val message: String? = null
)

data class DockerContainer(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val state: String,
    val ports: String? = null,
    val created: String? = null
)

data class DockerContainerDetail(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val state: String,
    val health: String? = null,
    val ports: List<String> = emptyList(),
    val startedAt: String? = null,
    val finishedAt: String? = null
)

data class DockerLogs(
    val container: String,
    val lines: List<String>
)

data class DockerComposeStatus(
    val project: String?,
    val services: List<DockerComposeService>
)

data class DockerComposeService(
    val name: String,
    val state: String,
    val status: String,
    val ports: String? = null
)
