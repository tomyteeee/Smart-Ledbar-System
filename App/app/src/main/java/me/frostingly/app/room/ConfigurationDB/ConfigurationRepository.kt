package me.frostingly.app.room.ConfigurationDB

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfigurationRepository(context: Context) {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
            CREATE TABLE IF NOT EXISTS configurations_new (
                id TEXT NOT NULL PRIMARY KEY,
                moments TEXT NOT NULL DEFAULT '[]'
            )
            """.trimIndent()
            )

            // Copy over IDs, defaulting moments to empty array
            database.execSQL(
                """
            INSERT INTO configurations_new (id, moments)
            SELECT id, '[]' FROM configurations
            """.trimIndent()
            )

            database.execSQL("DROP TABLE configurations")
            database.execSQL("ALTER TABLE configurations_new RENAME TO configurations")
        }
    }

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "configurations-db"
    ).addMigrations(MIGRATION_1_2)
        .build()

    private val configurationDao = db.configurationDao()

    suspend fun insertConfiguration(configuration: Configuration) {
        withContext(Dispatchers.IO) {
            configurationDao.insertConfiguration(configuration)
        }
    }

    suspend fun getAllConfigurations(): List<Configuration> {
        return withContext(Dispatchers.IO) {
            configurationDao.getAllConfigurations()
        }
    }

    suspend fun getConfigurationById(id: String): Configuration? {
        return withContext(Dispatchers.IO) {
            configurationDao.getConfigurationById(id)
        }
    }

    suspend fun clearAllConfigurations() {
        withContext(Dispatchers.IO) {
            configurationDao.clearAllConfigurations()
        }
    }

    // New method to remove a ledbar by ID
    suspend fun removeConfigurationById(id: String) {
        withContext(Dispatchers.IO) {
            configurationDao.removeConfigurationById(id)
        }
    }
    suspend fun updateConfiguration(updatedConfiguration: Configuration) {
        withContext(Dispatchers.IO) {
            configurationDao.updateConfiguration(updatedConfiguration)
        }
    }

    suspend fun clearDatabase() {
        withContext(Dispatchers.IO) {
            db.clearAllTables() // This will clear all data in all tables
        }
    }

}