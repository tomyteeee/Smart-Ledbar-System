package me.frostingly.app.room.ConfigurationDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ConfigurationDao {

    @Insert
    suspend fun insertConfiguration(configuration: Configuration)

    @Query("SELECT * FROM configurations")
    suspend fun getAllConfigurations(): List<Configuration>

    @Query("SELECT * FROM configurations WHERE id = :id LIMIT 1")
    suspend fun getConfigurationById(id: String): Configuration?

    @Query("DELETE FROM configurations")
    suspend fun clearAllConfigurations()

    @Query("DELETE FROM configurations WHERE id = :id")
    suspend fun removeConfigurationById(id: String)

    @Update
    suspend fun updateConfiguration(configuration: Configuration)
}