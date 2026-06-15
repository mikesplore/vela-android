package com.template.app.core.data.repository

import com.template.app.core.data.local.dao.UserDao
import com.template.app.core.data.remote.api.UserApiService
import com.template.app.core.utils.Resource
import com.template.app.core.utils.safeApiCall
import com.template.app.domain.model.User
import com.template.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first repository:
 *  - The UI always observes the local Room DB via [observeUsers]
 *  - [fetchUsers] refreshes from network and caches to DB
 *  - If network fails, the UI still shows cached data
 */
class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val userDao: UserDao
) : UserRepository {

    override fun observeUsers(): Flow<List<User>> =
        userDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun fetchUsers(): Resource<List<User>> {
        return safeApiCall {
            val users = apiService.getUsers()
            userDao.upsertAll(users.map { it.toEntity() }) // cache locally
            users.map { it.toEntity().toDomain() }
        }
    }

    override suspend fun getUserById(id: String): Resource<User> {
        return safeApiCall {
            apiService.getUserById(id)
                .toEntity()
                .also { userDao.upsert(it) } // cache individually
                .toDomain()
        }
    }

    override suspend fun createUser(displayName: String, email: String): Resource<User> {
        return safeApiCall {
            val body = mapOf("displayName" to displayName, "email" to email)
            val dto = apiService.createUser(body)
            val entity = dto.toEntity()
            userDao.upsert(entity)
            entity.toDomain()
        }
    }

    override suspend fun updateUser(id: String, displayName: String, email: String): Resource<User> {
        return safeApiCall {
            val body = mapOf("displayName" to displayName, "email" to email)
            val dto = apiService.updateUser(id, body)
            val entity = dto.toEntity()
            userDao.upsert(entity)
            entity.toDomain()
        }
    }

    override suspend fun deleteUser(id: String): Resource<Unit> {
        return safeApiCall {
            apiService.deleteUser(id)
            userDao.deleteById(id)
        }
    }
}
