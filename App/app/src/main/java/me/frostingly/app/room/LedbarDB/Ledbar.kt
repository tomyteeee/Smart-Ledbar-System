package me.frostingly.app.room.LedbarDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledbars")
data class Ledbar(
    @PrimaryKey val id: String,
    val mac_address: String,
    val name: String,
    val aukstas: Int,
    val configuration: String,
)
