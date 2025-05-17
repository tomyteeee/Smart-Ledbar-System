package me.frostingly.app.room.ConfigurationDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class Configuration(
    @PrimaryKey val id: String,
    val configuration: String,
)
