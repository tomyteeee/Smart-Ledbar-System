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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.frostingly.app.components.data.Effect
import me.frostingly.app.components.data.Moment

@Composable
fun LedbarPreviewUI(
    configuration: String,
) {
    // Total number of LEDs/groups
    val ledCount = 8

    val ledColors = remember(configuration) {
        parseColors(configuration, ledCount)
    }

    val ledEffects = remember(configuration) {
        parseEffects(configuration, ledCount)
    }

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
            ledColors.forEach { color ->
                LedGroupUI(color = color)
            }
        }
    }
}

fun parseMoments(configuration: String): List<Moment> {
    // Trim off "moments(" prefix and trailing ")"
    val inner = configuration
        .removePrefix("moments(")
        .removeSuffix(")")
        .trim()

    // Regex to match: id(delay,repeat): ... up to next id( or end
    val momentRegex = Regex("""(\d+)\((\d+)\s*,\s*(\d+)\)\s*:(.*?)(?=(\d+\()|$)""", RegexOption.DOT_MATCHES_ALL)
    return momentRegex.findAll(inner).mapNotNull { match ->
        val (idStr, delayStr, repeatStr, body) = match.destructured
        val id = idStr.toIntOrNull() ?: return@mapNotNull null
        val delay = delayStr.toIntOrNull() ?: return@mapNotNull null
        val repeat = repeatStr.toIntOrNull() ?: return@mapNotNull null

        // Split body by ';' and pick out colors= and effects=
        val parts = body
            .split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val colorParts = parts.filter { it.startsWith("colors=") || it.matches(Regex("""\d+-\d+:.*""")) }
        val colorPart = if (colorParts.isNotEmpty()) colorParts.joinToString(";") { it.trimEnd(';') } + ";" else ""

        val effectParts = parts.filter { it.startsWith("effects=") }
        val effectPart = if (effectParts.isNotEmpty()) effectParts.joinToString(";") { it.trimEnd(';') } + ";" else ""


        Moment(
            id = id,
            delayMs = delay,
            repeat = repeat,
            colorConfig = colorPart,
            effectConfig = effectPart
        )
    }.toList()
}

fun parseColors(configuration: String, ledCount: Int): List<Color> {
    val ledColors = MutableList(ledCount) { Color.White } // Default color

    try {
        val cleanedConfig = configuration.removePrefix("colors=").trim()
        val rules = cleanedConfig.split(";").map { it.trim() }.filter { it.isNotEmpty() }

        for (rule in rules) {
            val parts = rule.split(":", limit = 2)
            if (parts.size != 2) {
                Log.e("PROJEKTAS", "Invalid rule format: $rule")
                continue
            }

            val rangePart = parts[0].trim()
            val colorStr = parts[1].trim()

            // Parsing the color using the improved function
            val color = colorFromRgbStringUI(colorStr)
            Log.d("PROJEKTAS", "Color parsed: $color for ranges $rangePart")

            val ranges = rangePart.split(",").map { it.trim() }
            for (range in ranges) {
                if (range.contains("-")) {
                    val (start, end) = range.split("-").mapNotNull { it.toIntOrNull() }
                    for (i in start..end) {
                        if (i in 0 until ledCount) ledColors[i] = color
                    }
                } else {
                    val index = range.toIntOrNull()
                    if (index != null && index in 0 until ledCount) {
                        ledColors[index] = color
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("PROJEKTAS", "Failed to parse config: $configuration", e)
    }

    return ledColors
}

fun parseEffects(configuration: String, ledCount: Int): List<Effect> {
    val ledEffects = MutableList<Effect>(ledCount) { Effect.NONE }

    try {
        val cleanedConfig = configuration.removePrefix("effects=").trim()
        val rules = cleanedConfig.split(";").map { it.trim() }.filter { it.isNotEmpty() }

        for (rule in rules) {
            val parts = rule.split(":", limit = 2)
            if (parts.size != 2) {
                Log.e("PROJEKTAS", "Invalid rule format: $rule")
                continue
            }

            val rangePart = parts[0].trim()
            val effectStr = parts[1].trim()

            val effect = effectFromStringUI(effectStr)
            Log.d("PROJEKTAS", "Effect parsed: $effect for ranges $rangePart")

            val ranges = rangePart.split(",").map { it.trim() }
            for (range in ranges) {
                if (range.contains("-")) {
                    val (start, end) = range.split("-").mapNotNull { it.toIntOrNull() }
                    for (i in start..end) {
                        if (i in 0 until ledCount) ledEffects[i] = effect
                    }
                } else {
                    val index = range.toIntOrNull()
                    if (index != null && index in 0 until ledCount) {
                        ledEffects[index] = effect
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.e("PROJEKTAS", "Failed to parse effect config: $configuration", e)
    }

    return ledEffects
}

fun parseArgs(args: String): List<Int> {
    return args.split(",")
        .map { it.trim().toIntOrNull() ?: 0 } // Default to 0 if not a valid number
}

fun effectFromStringUI(effectStr: String): Effect {
    val parts = effectStr.trim().split("(", limit = 2)
    val name = parts[0].trim().lowercase()
    val argsStr = parts
        .getOrNull(1)
        ?.removeSuffix(")")
        ?: ""

    return when (name) {
        "none"   -> Effect.NONE
        "blink" -> {
            val parsedArgs = parseArgs(argsStr)
            val delay = parsedArgs.getOrNull(0) ?: 500
            val times = parsedArgs.getOrNull(1) ?: 1
            Effect.Blink(delay, times)
        }
        "wave" -> {
            val parsedArgs = parseArgs(argsStr)
            val delay = parsedArgs.getOrNull(0) ?: 500
            val speed = parsedArgs.getOrNull(1) ?: 500
            val times = parsedArgs.getOrNull(2) ?: 1
            Effect.Wave(delay, speed, times)
        }

        else     -> {
            Log.w("PROJEKTAS", "Unknown effect '$effectStr', using NONE")
            Effect.NONE
        }
    }
}


fun colorFromRgbStringUI(rgbString: String): Color {
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
