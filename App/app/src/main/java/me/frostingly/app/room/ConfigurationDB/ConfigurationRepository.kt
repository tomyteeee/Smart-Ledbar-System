package me.frostingly.app.room.ConfigurationDB

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfigurationRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "configurations-db"
    )
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