package me.frostingly.app.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import me.frostingly.app.bluetooth.BluetoothManagerSingleton
import sendBluetoothCommand
import kotlin.math.roundToInt

@Composable
fun RGBColorPicker(
    context: Context,
    rgbColorStr: String,
    selectedGroupIndices: Set<Int>,
    onColorChange: (String) -> Unit
) {
    // Set default position to top-right
    var hue by remember { mutableStateOf(180f) }
    var saturation by remember { mutableStateOf(1f) } // Full saturation initially
    var brightness by remember { mutableStateOf(1f) } // Full brightness initially
    var selectedX by remember { mutableStateOf(570f) } // Top-right position
    var selectedY by remember { mutableStateOf(0f) }   // Top of the picker (max brightness)

    val baseColor = Color.hsv(hue, 1f, 1f) // Base color for the hue
    var rgbColor by remember { mutableStateOf(Color.hsv(hue, saturation, brightness)) }

    val bluetoothManager = BluetoothManagerSingleton.getInstance(context)

    LaunchedEffect(selectedGroupIndices) {
        Log.d(
            "PROJEKTAS",
            "RGBColorPicker recomposed with selectedGroupIndices: $selectedGroupIndices"
        )
    }

    val currentSelectedGroupIndices = rememberUpdatedState(selectedGroupIndices)

    fun updateRGBString() {
        // Calculate the RGB color based on the current dot position
        val r = (rgbColor.red * 255).roundToInt()
        val g = (rgbColor.green * 255).roundToInt()
        val b = (rgbColor.blue * 255).roundToInt()
        val rgbString = "$r $g $b"
        onColorChange(rgbString) // Update the parent component

        if (currentSelectedGroupIndices.value.isNotEmpty()) {
            sendBluetoothCommand(
                context,
                "gc([${reorganizeIndices(currentSelectedGroupIndices.value)}], \"$rgbString\")\n"
            ) { }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(start = 6.dp, end = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Box(
            modifier = Modifier
                .size(285.dp, 200.dp) // Set the size of the color picker
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            selectedX = offset.x.coerceIn(0f, 570f) // Horizontal position based on x
                            selectedY = offset.y.coerceIn(0f, 400f) // Vertical position based on y
                            saturation = selectedX / 570f // Map x to saturation
                            brightness = 1 - (selectedY / 400f) // Map y to brightness
                            rgbColor = Color.hsv(hue, saturation, brightness)
                            updateRGBString() // Update the color string
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            updateRGBString() // Update when drag ends
                        }
                    ) { _, dragAmount ->
                        selectedX = (selectedX + dragAmount.x).coerceIn(0f, 570f)
                        selectedY = (selectedY + dragAmount.y).coerceIn(0f, 400f)
                        saturation = selectedX / 570f
                        brightness = 1 - (selectedY / 400f)
                        rgbColor = Color.hsv(hue, saturation, brightness)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, baseColor) // Gradient based on hue
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black) // Gradient for brightness
                    )
                )
                val dotColor = if (brightness > 0.5) Color.Black else Color.White
                drawCircle(dotColor, radius = 8f, center = Offset(selectedX, selectedY)) // Dot position
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Hue Selector
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp), // Limit the width of the hue selector
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VerticalSpectrumSlider(
                hue = hue,
                onValueChange = { newHue ->
                    hue = newHue
                    // Recalculate the RGB color based on the new hue
                    rgbColor = Color.hsv(hue, saturation, brightness)
                },
                onDragEndFn = {
                    updateRGBString() // Update the color when dragging ends
                }
            )
        }
    }
}

@Composable
fun VerticalSpectrumSlider(hue: Float, onValueChange: (Float) -> Unit, onDragEndFn: () -> Unit) {
    var sliderY by remember { mutableStateOf((hue / 360) * 200) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .width(40.dp)
                .background(
                    Brush.verticalGradient(
                        colors = List(360) { Color.hsv(it.toFloat(), 1f, 1f) }
                    )
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            onDragEndFn()
                        }
                    ) { _, dragAmount ->
                        sliderY = (sliderY + dragAmount).coerceIn(0f, 400f)
                        val newValue = (sliderY / 400 * 360).coerceIn(0f, 360f)
                        onValueChange(newValue)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            sliderY = offset.y.coerceIn(0f, 400f)
                            val newValue = (sliderY / 400 * 360).coerceIn(0f, 360f)
                            onValueChange(newValue)
                            onDragEndFn()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, sliderY),
                    end = Offset(size.width, sliderY),
                    strokeWidth = 4f
                )
            }
        }
    }
}



fun reorganizeIndices(indices: Set<Int>): String {
    val sortedIndices = indices.sorted()

    val ranges = mutableListOf<String>()
    var rangeStart = sortedIndices[0]
    var rangeEnd = sortedIndices[0]

    for (i in 1 until sortedIndices.size) {
        if (sortedIndices[i] == rangeEnd + 1) {
            rangeEnd = sortedIndices[i]
        } else {
            ranges.add(if (rangeStart == rangeEnd) {
                rangeStart.toString()
            } else {
                "$rangeStart-$rangeEnd"
            })
            rangeStart = sortedIndices[i]
            rangeEnd = sortedIndices[i]
        }
    }

    ranges.add(if (rangeStart == rangeEnd) {
        rangeStart.toString()
    } else {
        "$rangeStart-$rangeEnd"
    })

    return ranges.joinToString(", ")
}