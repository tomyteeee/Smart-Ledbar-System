package me.frostingly.app.room.ConfigurationDB

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Configuration::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun configurationDao(): ConfigurationDao
}