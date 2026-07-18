package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.VelaDao
import com.template.app.core.data.local.entities.VelaMediaEntity
import com.template.app.core.data.remote.api.VelaApiService
import com.template.app.core.data.remote.dto.MediaSeekRequest
import com.template.app.core.device.ActiveConnectionProvider
import com.template.app.core.device.scopedNullable
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.VelaMediaState
import com.template.app.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val apiService: VelaApiService,
    private val velaDao: VelaDao,
    private val activeConnection: ActiveConnectionProvider,
) : MediaRepository {

    override fun observeMedia(): Flow<VelaMediaState?> =
        activeConnection.scopedNullable { id ->
            velaDao.observeMedia(id).map { it?.toDomain() }
        }

    override suspend fun getNowPlaying(): Resource<VelaMediaState?> = safeApiCall {
        val connectionId = activeConnection.requireActiveId()
        apiService.getNowPlaying().let {
            val domain = VelaMediaState(
                title = it.title,
                artist = it.artist,
                album = it.album,
                status = it.status,
                positionSeconds = it.positionSeconds,
                lengthSeconds = it.lengthSeconds,
                artUrl = it.artUrl
            )
            velaDao.upsertMedia(VelaMediaEntity.fromDomain(connectionId, domain))
            domain
        }
    }

    override suspend fun togglePlayPause(): Resource<Unit> = safeApiCall {
        apiService.togglePlayPause()
        Unit
    }

    override suspend fun mediaNext(): Resource<Unit> = safeApiCall {
        apiService.mediaNext()
        Unit
    }

    override suspend fun mediaPrevious(): Resource<Unit> = safeApiCall {
        apiService.mediaPrevious()
        Unit
    }

    override suspend fun mediaSeek(seconds: Int): Resource<Unit> = safeApiCall {
        apiService.mediaSeek(MediaSeekRequest(seconds))
        Unit
    }
}
