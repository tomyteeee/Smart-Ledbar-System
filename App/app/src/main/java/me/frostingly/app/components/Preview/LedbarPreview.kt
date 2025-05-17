package me.frostingly.app.components.Preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.frostingly.app.components.data.Effect

@Composable
fun LedbarPreview(
    configuration: String,
    groupColors: Map<Int, String>,
    groupEffects: Map<Int, Effect>,
    selectedGroupIndices: Set<Int>,
    onGroupSelected: (Set<Int>) -> Unit,
    rgbColorStr: String,
    displayMomentsAndEffects: Boolean,
) {
    val visibilityMap = remember { mutableStateMapOf<Int, Boolean>() }
    var fadeAlpha by remember { mutableStateOf(1f) }
    var currentColor by remember { mutableStateOf(rgbColorStr) }
    var triggerAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(rgbColorStr) {
        currentColor = rgbColorStr
    }

    LaunchedEffect(selectedGroupIndices) {
        if (selectedGroupIndices.isNotEmpty()) {
            while (true) {
                delay(200) // Delay before fading out
                fadeAlpha = 0.3f // Fade out
                delay(200) // Delay before fading in
                fadeAlpha = 1f // Fade in
            }
        }
    }

    LaunchedEffect(groupEffects) {
        visibilityMap.clear()
        for (i in 0 until 8) visibilityMap[i] = true

        if (displayMomentsAndEffects) {
            groupEffects.forEach { (idx, effect) ->
                if (effect is Effect.Blink) {
                    launch {
                        repeat(effect.times) {
                            visibilityMap[idx] = false
                            delay(effect.delay.toLong())
                            visibilityMap[idx] = true
                            delay(effect.delay.toLong())
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.size(350.dp, 50.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { /* hide colors */ },
            border = BorderStroke(2.dp, Color(7, 53, 139)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text("Slėpti spalvas", fontSize = 12.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedButton(
            onClick = { /* hide effects */ },
            border = BorderStroke(2.dp, Color(7, 53, 139)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
            modifier = Modifier.height(28.dp)
        ) {
            Text("Slėpti momentus", fontSize = 12.sp, color = Color.White)
        }
    }

    Box(
        modifier = Modifier
            .size(350.dp, 50.dp)
            .border(2.dp, Color.Black)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0 until 8).forEach { index ->
                val baseColor = colorFromRgbString(groupColors[index] ?: "255 255 255")
                val visible = visibilityMap[index] ?: true
                LedGroup(
                    color = if (!displayMomentsAndEffects || visible) baseColor else baseColor.copy(alpha = 0.2f),
                    groupIndex = index,
                    isSelected = selectedGroupIndices.contains(index),
                    onClick = { groupIndex ->
                        val updatedSelection = if (selectedGroupIndices.contains(groupIndex)) {
                            selectedGroupIndices - groupIndex
                        } else {
                            selectedGroupIndices + groupIndex
                        }
                        onGroupSelected(updatedSelection)
                    },
                    fadeAlpha = fadeAlpha
                )
            }
        }
    }

    Row(
        modifier = Modifier.size(350.dp, 50.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { onGroupSelected(emptySet()) },
            border = BorderStroke(2.dp, Color(7, 53, 139)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
            modifier = Modifier.height(28.dp)
        ) {
            Text("Anuliuoti visus", fontSize = 12.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedButton(
            onClick = { onGroupSelected((0..7).toSet()) },
            border = BorderStroke(2.dp, Color(7, 53, 139)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
            modifier = Modifier.height(28.dp)
        ) {
            Text("Pasirinkti visus", fontSize = 12.sp, color = Color.White)
        }
    }
}

fun colorFromRgbString(rgbString: String): Color {
    val rgbComponents = rgbString.split(" ")
    if (rgbComponents.size != 3) throw IllegalArgumentException("Invalid RGB string. Must contain 3 values.")
    val r = rgbComponents[0].toInt()
    val g = rgbComponents[1].toInt()
    val b = rgbComponents[2].toInt()
    return Color(r / 255f, g / 255f, b / 255f)
}
