package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun observeMedia(): Flow<VelaMediaState?>
    suspend fun getNowPlaying(): Resource<VelaMediaState?>
    suspend fun togglePlayPause(): Resource<Unit>
    suspend fun mediaNext(): Resource<Unit>
    suspend fun mediaPrevious(): Resource<Unit>
    suspend fun mediaSeek(seconds: Int): Resource<Unit>
}
