package me.frostingly.app.room.ConfigurationDB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.frostingly.app.components.data.Converters

@Database(entities = [Configuration::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun configurationDao(): ConfigurationDao
}