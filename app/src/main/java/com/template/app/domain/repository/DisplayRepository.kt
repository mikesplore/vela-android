package com.template.app.domain.repository
import com.template.app.core.utils.Resource
import com.template.app.domain.model.VelaBrightness
import com.template.app.domain.model.VelaResolution
import kotlinx.coroutines.flow.Flow

interface DisplayRepository {

    fun observeBrightness(): Flow<VelaBrightness?>

    fun observeResolution(): Flow<VelaResolution?>
    suspend fun getScreenshot(): Resource<String> // base64
    suspend fun setBrightness(value: Int): Resource<Unit>
    suspend fun lockDisplay(): Resource<Unit>
    suspend fun getResolution(): Resource<String>
    suspend fun getBrightness(): Resource<Int>
    suspend fun monitorOff(): Resource<Unit>
    suspend fun monitorOn(): Resource<Unit>
    suspend fun rotateDisplay(orientation: String): Resource<Unit>
    suspend fun setNightLight(enabled: Boolean, temperature: Int? = null): Resource<Unit>
    suspend fun recordDisplay(durationSeconds: Int): Resource<String> // base64
}
