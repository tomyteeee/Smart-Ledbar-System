package me.frostingly.app.room.LedbarDB

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Ledbar::class], version = 2)
abstract class AppDatabase: RoomDatabase() {
    abstract fun ledbarDao(): LedbarDao
}