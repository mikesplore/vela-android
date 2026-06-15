package com.template.app.domain.repository

import com.template.app.core.utils.Resource
import com.template.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract defined in the domain layer.
 * The data layer provides the implementation; Hilt injects it.
 * The domain/UI layers only ever depend on this interface — never the impl.
 */
interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun fetchUsers(): Resource<List<User>>
    suspend fun getUserById(id: String): Resource<User>
    suspend fun createUser(displayName: String, email: String): Resource<User>
    suspend fun updateUser(id: String, displayName: String, email: String): Resource<User>
    suspend fun deleteUser(id: String): Resource<Unit>
}
