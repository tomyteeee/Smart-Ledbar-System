package me.frostingly.app.components.Checkboxes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.frostingly.app.components.NumberField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlinkEffect(
    enabled: Boolean,
    delayMs: String,
    times: String,
    onEnabledChange: (Boolean) -> Unit,
    onDelayChange: (String) -> Unit,
    onTimesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = enabled,
                onCheckedChange = onEnabledChange,
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
            Spacer(Modifier.width(8.dp))
            Text("Mirksėjimo efektas")
        }
        if (enabled) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                // Delay field
                NumberField(
                    value = delayMs,
                    placeholder = "Palaukti",
                    unit = "ms",
                    onValueChange = onDelayChange
                )
                // Times field
                NumberField(
                    value = times,
                    placeholder = "Kartoti",
                    unit = "kartus",
                    onValueChange = onTimesChange
                )
                Button(onClick = {
                    onSave()
                },                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(11, 77, 199)
                ),) {
                    Text("IŠSAUGOTI")
                }
            }
        }
    }
}


