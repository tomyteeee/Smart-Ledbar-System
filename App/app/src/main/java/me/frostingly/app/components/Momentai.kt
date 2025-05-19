package me.frostingly.app.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import me.frostingly.app.components.data.ColorConfig
import me.frostingly.app.components.data.Moment
import me.frostingly.app.room.ConfigurationDB.Configuration
import kotlin.Int

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Momentai(configuration: MutableState<Configuration>, selectedIndex: Int, onSelectedIndex: (Int) -> Unit) {

    var items by remember { mutableStateOf(configuration.value.moments) }

    val safeIndex = selectedIndex.coerceIn(0, configuration.value.moments.lastIndex)

    var delay by remember(selectedIndex) {
        mutableStateOf(configuration.value.moments.getOrNull(safeIndex ?: -1)?.delayMs?.toString() ?: "")
    }
    var times by remember(selectedIndex) {
        mutableStateOf(configuration.value.moments.getOrNull(safeIndex ?: -1)?.repeat?.toString() ?: "")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(325.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .border(BorderStroke(2.dp, Color(7, 53, 139)), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
                .verticalScroll(rememberScrollState())
        ) {
            items.forEachIndexed { index, moment ->
                val isSelected = safeIndex == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 4.dp else 2.dp,
                            color = if (isSelected) Color(7, 53, 139) else Color.Black,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable {
                            onSelectedIndex(index)
                        }
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Momentas ${index + 1}")
                    }
                    IconButton(onClick = {
                        if (items.size > 1) {
                            val newList = items.toMutableList().also { it.removeAt(index) }
                            items = newList
                            val updatedMoments = configuration.value.moments.toMutableList().also { it.removeAt(index) }
                            configuration.value = configuration.value.copy(moments = updatedMoments)
                            val newSelectedIndex = when {
                                newList.size == 1 -> 0
                                safeIndex == index -> newList.lastIndex
                                safeIndex > index -> safeIndex - 1
                                else -> safeIndex
                            }

                            onSelectedIndex(newSelectedIndex)
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(11, 77, 199))
                    }
                }

                if (isSelected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 6.dp, end = 6.dp)
                    ) {
                        // Speed field
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(32.dp)
                                .background(Color(11, 77, 199), shape = RoundedCornerShape(8.dp))
                                .border(2.dp, Color(7, 53, 139), shape = RoundedCornerShape(8.dp)),
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
                                    if (it.isDigitsOnly() && it.length <= 5) {
                                        delay = it
                                        if (it.isNotEmpty()) {
                                            val updatedMoment = configuration.value.moments[selectedIndex!!].copy(delayMs = it.toInt())
                                            val updatedMoments = configuration.value.moments.toMutableList().also { list ->
                                                list[selectedIndex!!] = updatedMoment
                                            }
                                            configuration.value = configuration.value.copy(moments = updatedMoments)
                                        }
                                    }
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
                                .background(Color(11, 77, 199), shape = RoundedCornerShape(8.dp))
                                .border(2.dp, Color(7, 53, 139), shape = RoundedCornerShape(8.dp)),
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
                                    if (it.isDigitsOnly() && it.length <= 5) {
                                        times = it
                                        if (it.isNotEmpty()) {
                                            val updatedMoment = configuration.value.moments[safeIndex].copy(repeat = it.toInt())
                                            val updatedMoments = configuration.value.moments.toMutableList().also { list ->
                                                list[safeIndex] = updatedMoment
                                            }
                                            configuration.value = configuration.value.copy(moments = updatedMoments)
                                        }
                                    }
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedButton(
                onClick = {
                    if (items.size < 5) {
                        var moment = Moment(items.size + 1, 1000, 1, listOf(
                            ColorConfig(0, "255,255,255"),
                            ColorConfig(1, "255,255,255"),
                            ColorConfig(2, "255,255,255"),
                            ColorConfig(3, "255,255,255"),
                            ColorConfig(4, "255,255,255"),
                            ColorConfig(5, "255,255,255"),
                            ColorConfig(6, "255,255,255"),
                            ColorConfig(7, "255,255,255"),
                        ), listOf())
                        Log.d("PROJEKTAS", moment.toString())
                        items = items + moment
                        val updatedItems = configuration.value.moments + moment
                        configuration.value = configuration.value.copy(moments = updatedItems)
                    }
                },
                border = BorderStroke(2.dp, Color(7, 53, 139)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(11, 77, 199),
                ),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("+",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
}