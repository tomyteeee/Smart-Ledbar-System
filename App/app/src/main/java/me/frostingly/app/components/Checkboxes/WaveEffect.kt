package me.frostingly.app.components.Checkboxes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveEffect() {
    var isMoveEffectEnabled by remember { mutableStateOf(false) }
    var delay by remember { mutableStateOf("") }
    var speed by remember { mutableStateOf("") }
    var times by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Checkbox for Move Effect
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isMoveEffectEnabled,
                onCheckedChange = { isChecked ->
                    isMoveEffectEnabled = isChecked
                    if (!isChecked) {
                        // Clear fields when unchecked
                        speed = ""
                        times = ""
                    }
                },
                colors = CheckboxColors(
                    checkedCheckmarkColor = Color.White,
                    checkedBoxColor = Color(11, 77, 199),
                    checkedBorderColor = Color(7, 53, 139),

                    uncheckedCheckmarkColor = Color.Transparent, // usually no checkmark when unchecked
                    uncheckedBoxColor = Color.White,

                    disabledCheckedBoxColor = Color(7, 53, 139).copy(alpha = 0.3f),
                    disabledUncheckedBoxColor = Color.LightGray.copy(alpha = 0.3f),
                    disabledIndeterminateBoxColor = Color.Gray.copy(alpha = 0.3f),

                    uncheckedBorderColor = Color(11, 77, 199),

                    disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledUncheckedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    disabledIndeterminateBorderColor = Color.Gray.copy(alpha = 0.5f)
                )

            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bangavimo efektas")
        }

        // Display "speed" and "times" fields side by side if checkbox is checked
        if (isMoveEffectEnabled) {

            // Row to align speed and times side by side
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 6.dp, end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp)
                        .background(Color(11, 77, 199), shape = RoundedCornerShape(4.dp))
                        .border(2.dp, Color(7, 53, 139), shape = RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (delay.isEmpty()) {
                        Text(
                            text = "Palaukti",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    BasicTextField(
                        value = delay,
                        onValueChange = {
                            if (it.isDigitsOnly() && it.length <= 5)
                                delay = it
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        cursorBrush = SolidColor(Color.White)
                    )
                }

                Text("ms")

                // Speed field
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp)
                        .background(Color(11, 77, 199), shape = RoundedCornerShape(4.dp))
                        .border(2.dp, Color(7, 53, 139), shape = RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (speed.isEmpty()) {
                        Text(
                            text = "Greitis",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    BasicTextField(
                        value = speed,
                        onValueChange = {
                            if (it.isDigitsOnly() && it.length <= 5)
                                speed = it
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        cursorBrush = SolidColor(Color.White)
                    )
                }

                Text("ms")


                // Times field
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp)
                        .background(Color(11, 77, 199), shape = RoundedCornerShape(4.dp))
                        .border(2.dp, Color(7, 53, 139), shape = RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (times.isEmpty()) {
                        Text(
                            text = "Kartoti",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    BasicTextField(
                        value = times,
                        onValueChange = {
                            if (it.isDigitsOnly() && it.length <= 5)
                                times = it
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        cursorBrush = SolidColor(Color.White)
                    )
                }

                Text("kartus")

            }
        }
    }
}