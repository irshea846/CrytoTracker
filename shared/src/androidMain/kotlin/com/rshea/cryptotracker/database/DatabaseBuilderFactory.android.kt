package com.rshea.cryptotracker.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class DatabaseBuilderFactory(
    private val context: Context // Android context required to resolve absolute file paths
) {
    actual fun createBuilder(): RoomDatabase.Builder<AppDatabase> {
        val appContext = context.applicationContext
        val dbFile: File = appContext.getDatabasePath("room_crypto.db")

        return Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}