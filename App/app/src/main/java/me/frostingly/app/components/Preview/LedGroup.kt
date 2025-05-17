package me.frostingly.app.components.Preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LedGroup(
    color: Color,
    groupIndex: Int,
    isSelected: Boolean,
    onClick: (Int) -> Unit,
    fadeAlpha: Float
) {
    Box(
        modifier = Modifier
            .size(40.dp, 50.dp)
            .clickable { onClick(groupIndex) }, // Trigger onClick on selection toggle
        contentAlignment = Alignment.Center

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Led(color, fadeAlpha, isSelected) // Only show border when selected
            Led(color, fadeAlpha, isSelected) // Only show border when selected
            Led(color, fadeAlpha, isSelected) // Only show border when selected
        }
    }
}

