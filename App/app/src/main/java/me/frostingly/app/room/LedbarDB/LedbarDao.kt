package me.frostingly.app.room.LedbarDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LedbarDao {

    @Insert
    suspend fun insertLedbar(ledbar: Ledbar)

    @Query("SELECT * FROM ledbars")
    suspend fun getAllLedbars(): List<Ledbar>

    @Query("SELECT * FROM ledbars WHERE id = :id LIMIT 1")
    suspend fun getLedbarById(id: String): Ledbar?

    @Query("DELETE FROM ledbars")
    suspend fun clearAllLedbars()

    @Query("DELETE FROM ledbars WHERE id = :id")
    suspend fun removeLedbarById(id: String)

    @Update
    suspend fun updateLedbar(ledbar: Ledbar)
}