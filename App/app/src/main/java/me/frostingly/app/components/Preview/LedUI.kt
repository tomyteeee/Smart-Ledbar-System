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
fun LedUI(color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(9.5.dp)
            .background(
                color = color, // Keep the inner circle fully opaque
                shape = CircleShape
            )
            .border(1.dp, Color.White, CircleShape)
    ) {

    }
}



