package me.frostingly.app.components.Preview

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.frostingly.app.components.data.Effect
import me.frostingly.app.room.ConfigurationDB.Configuration

@Composable
fun LedbarPreview(
    configuration: Configuration,
    groupColors: Map<Int, String>,
    groupEffects: Map<Int, Effect>,
    selectedGroupIndices: Set<Int>,
    onGroupSelected: (Set<Int>) -> Unit,
    rgbColorStr: String,
    displayMoments: Boolean,
    displayEffects: Boolean,
    onToggleMoments: () -> Unit,
    onToggleEffects: () -> Unit,
    loopKey: Int,
    selectedMoment: Int?,
    visibilityMap: SnapshotStateMap<Int, Boolean>,
) {
    var fadeAlpha by remember { mutableStateOf(1f) }
    var currentColor by remember { mutableStateOf(rgbColorStr) }
    var currentlySelectedMoment by remember { mutableStateOf(selectedMoment) }

    LaunchedEffect(rgbColorStr) {
        currentColor = rgbColorStr
    }

    LaunchedEffect(selectedMoment) {
        currentlySelectedMoment = selectedMoment
    }

    LaunchedEffect(selectedGroupIndices) {
        if (selectedGroupIndices.isNotEmpty()) {
            while (true) {
                delay(200)
                fadeAlpha = 0.3f
                delay(200)
                fadeAlpha = 1f
            }
        }
    }

    val colorsToUse = remember(displayMoments, selectedMoment, groupColors) {
        if (!displayMoments && selectedMoment != null) {
            configuration.moments
                .getOrNull(selectedMoment)
                ?.colorConfig
                ?.associate { it.index to it.rgb }
                ?: groupColors
        } else {
            groupColors
        }
    }

    Row(
        modifier = Modifier.size(350.dp, 50.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedButton(
            onClick = {
                onToggleEffects()
            },
            border = BorderStroke(2.dp, Color(7, 53, 139)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
            modifier = Modifier.height(28.dp)
        ) {
            Text(if (displayEffects) "Slėpti efektus" else "Rodyti efektus", fontSize = 12.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.width(6.dp))
        OutlinedButton(
            onClick = {
                onToggleMoments()
                fadeAlpha = 1f
            },
            border = BorderStroke(2.dp, Color(7, 53, 139)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text(if (displayMoments) "Slėpti momentus" else "Rodyti momentus", fontSize = 12.sp, color = Color.White)
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
                val baseColor = colorFromRgbString(colorsToUse[index] ?: "255,255,255")
                val visible = visibilityMap[index] ?: true
                val shouldDim = displayEffects && !visible
                LedGroup(
                    color = if (shouldDim) baseColor.copy(alpha = 0.2f) else baseColor,
                    groupIndex = index,
                    isSelected = selectedGroupIndices.contains(index),
                    onClick = { groupIndex ->
                        val updated = if (selectedGroupIndices.contains(groupIndex)) selectedGroupIndices - groupIndex else selectedGroupIndices + groupIndex
                        onGroupSelected(updated)
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
            Text("Atmesti visus", fontSize = 12.sp, color = Color.White)
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
