package me.frostingly.app.components.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.frostingly.app.room.ConfigurationDB.Configuration

class Converters {
    @TypeConverter
    fun fromMomentList(value: List<Moment>): String = Json.encodeToString(value)

    @TypeConverter
    fun toMomentList(value: String): List<Moment> = Json.decodeFromString(value)

    @TypeConverter
    fun fromConfiguration(value: Configuration): String = Json.encodeToString(value)

    @TypeConverter
    fun toConfiguration(value: String): Configuration = Json.decodeFromString(value)

    @TypeConverter
    fun fromEffectList(value: List<Effect>): String = Json.encodeToString(value)

    @TypeConverter
    fun toEffectList(value: String): List<Effect> = Json.decodeFromString(value)

    @TypeConverter
    fun fromColorConfigList(value: List<ColorConfig>): String = Json.encodeToString(value)

    @TypeConverter
    fun toColorConfigList(value: String): List<ColorConfig> = Json.decodeFromString(value)
}
