package me.frostingly.app.room.LedbarDB

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LedbarRepository(context: Context) {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ledbars ADD COLUMN configuration TEXT NOT NULL DEFAULT '-'")
        }
    }


    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Replace the old default "-" with a valid JSON for your default Configuration.
            // Here we assume your default Configuration() serializes to "{}"
            db.execSQL(
                """
            UPDATE ledbars 
               SET configuration = '{}' 
             WHERE configuration = '-'
            """.trimIndent()
            )
            // (If your default config JSON is more complex, paste it in place of '{}')
        }
    }

        private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "ledbars-db"
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()

    private val ledbarDao = db.ledbarDao()

    suspend fun insertLedbar(ledbar: Ledbar) {
        withContext(Dispatchers.IO) {
            ledbarDao.insertLedbar(ledbar)
        }
    }

    suspend fun getAllLedbars(): List<Ledbar> {
        return withContext(Dispatchers.IO) {
            ledbarDao.getAllLedbars()
        }
    }

    suspend fun getLedbarById(id: String): Ledbar? {
        return withContext(Dispatchers.IO) {
            ledbarDao.getLedbarById(id)
        }
    }

    suspend fun clearAllLedbars() {
        withContext(Dispatchers.IO) {
            ledbarDao.clearAllLedbars()
        }
    }

    // New method to remove a ledbar by ID
    suspend fun removeLedbarById(id: String) {
        withContext(Dispatchers.IO) {
            ledbarDao.removeLedbarById(id)
        }
    }
    suspend fun updateLedbar(updatedLedbar: Ledbar) {
        withContext(Dispatchers.IO) {
            ledbarDao.updateLedbar(updatedLedbar)
        }
    }

    suspend fun clearDatabase() {
        withContext(Dispatchers.IO) {
            db.clearAllTables() // This will clear all data in all tables
        }
    }

}