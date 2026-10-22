package com.rshea.cryptotracker.database

import androidx.room.TypeConverter

class RoomTypeConverters {

    // FIX: Converts a primitive Long numeric timestamp to a standard SQLite String representation
    @TypeConverter
    fun fromLongToString(value: Long?): String {
        return value?.toString() ?: ""
    }

    // FIX: Converts a standard SQLite String representation back into a type-safe Long primitive
    @TypeConverter
    fun fromStringToLong(value: String?): Long {
        return value?.toLongOrNull() ?: 0L
    }
}
