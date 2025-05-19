package me.frostingly.app.room.ConfigurationDB

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import me.frostingly.app.components.data.Moment

@Entity(tableName = "configurations")
@Serializable
data class Configuration(
    @PrimaryKey val id: String,
    val moments: List<Moment>
)
