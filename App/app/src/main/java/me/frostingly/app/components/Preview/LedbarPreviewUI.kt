package me.frostingly.app.components.Preview

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.frostingly.app.room.ConfigurationDB.Configuration

@Composable
fun LedbarPreviewUI(
    configuration: Configuration,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(2.dp, Color.Black)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            configuration.moments[0].colorConfig.forEach { colorConfig ->
                LedGroupUI(colorFromRgbString(colorConfig.rgb))
            }
        }
    }
}


fun colorFromRgbString(rgbString: String): Color {
    // Split the string into components (no quotes, just numbers separated by commas)
    val rgbComponents = rgbString.split(",").map { it.trim() }

    if (rgbComponents.size != 3) {
        throw IllegalArgumentException("Invalid RGB string. Must contain exactly 3 comma-separated values: '$rgbString'")
    }

    try {
        val r = rgbComponents[0].toInt().coerceIn(0, 255)
        val g = rgbComponents[1].toInt().coerceIn(0, 255)
        val b = rgbComponents[2].toInt().coerceIn(0, 255)

        return Color(r / 255f, g / 255f, b / 255f)
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("RGB values must be integers: '$rgbString'", e)
    }
}

fun colorToRgbString(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return "$r $g $b"
}
