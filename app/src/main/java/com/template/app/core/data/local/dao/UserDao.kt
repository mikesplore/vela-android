package com.template.app.core.data.local.dao

import androidx.room.*
import com.template.app.core.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun observeAll(): Flow<List<UserEntity>>          // Use Flow for reactive UI updates

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Upsert                                           // Insert or update (Room 2.5+)
    suspend fun upsert(user: UserEntity)

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
