package com.rshea.cryptotracker.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSString
import platform.Foundation.stringByAppendingPathComponent

actual class DatabaseBuilderFactory {
    actual fun createBuilder(): RoomDatabase.Builder<AppDatabase> {
        // Safe, native Apple Silicon sandbox path retrieval logic
        val homeDir = NSHomeDirectory()
        val documentDir = (homeDir as NSString).stringByAppendingPathComponent("Documents")
        val dbPath = (documentDir as NSString).stringByAppendingPathComponent("room_crypto.db")

        return Room.databaseBuilder<AppDatabase>(
            name = dbPath,
            factory =  { AppDatabaseConstructor.initialize() } // Hooks into our KSP generated actual object
        )
    }
}
