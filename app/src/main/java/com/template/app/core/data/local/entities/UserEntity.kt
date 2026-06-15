package com.template.app.core.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.template.app.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
) {
    /** Maps DB entity → domain model. Keep domain models free of Room annotations. */
    fun toDomain() = User(
        id = id,
        displayName = displayName,
        email = email,
        avatarUrl = avatarUrl
    )
}
