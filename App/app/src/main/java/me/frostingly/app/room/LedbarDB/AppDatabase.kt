package me.frostingly.app.room.LedbarDB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.frostingly.app.components.data.Converters

@Database(entities = [Ledbar::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun ledbarDao(): LedbarDao
}