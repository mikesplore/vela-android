package com.template.app.core.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Add converters here for any non-primitive types Room can't store directly
 * (e.g. Date, List<String>, custom enums, etc.)
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStringList(value: String?): List<String> =
        value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()

    @TypeConverter
    fun toStringList(list: List<String>?): String =
        list?.joinToString(",") ?: ""
}
