package me.frostingly.app.components.Preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Led(color: Color, fadeAlpha: Float, isSelected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(11.dp)
            .then(
                // Apply the border only when isSelected is true
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.Blue.copy(alpha = fadeAlpha), // Blue border with fade effect
                        shape = CircleShape
                    )
                } else {
                    Modifier // If not selected, no border
                }
            )
            .background(
                color = color, // Keep the inner circle fully opaque
                shape = CircleShape
            )
    ) {

    }
}



