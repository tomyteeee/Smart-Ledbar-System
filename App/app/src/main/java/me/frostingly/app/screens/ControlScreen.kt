package me.frostingly.app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.frostingly.app.R
import me.frostingly.app.Screen
import me.frostingly.app.SharedPreferences
import me.frostingly.app.bluetooth.BluetoothManagerSingleton
import me.frostingly.app.bluetooth.ConnectionStatus
import me.frostingly.app.components.Checkboxes.BlinkEffect
import me.frostingly.app.components.Checkboxes.MoveEffect
import me.frostingly.app.components.Checkboxes.PulseEffect
import me.frostingly.app.components.Checkboxes.WaveEffect
import me.frostingly.app.components.Preview.LedbarPreview
import me.frostingly.app.components.Momentai
import me.frostingly.app.components.RGBColorPicker
import me.frostingly.app.components.data.Effect
import me.frostingly.app.room.ConfigurationDB.Configuration
import me.frostingly.app.room.ConfigurationDB.ConfigurationRepository
import me.frostingly.app.room.LedbarDB.LedbarRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import sendBluetoothCommand

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@SuppressLint("MissingPermission")
@Composable
fun ControlScreen(
    navController: NavController,
    access_code: String,
    sharedPreferences: SharedPreferences,
    context: Context,
    ledbarRepository: LedbarRepository,
    configurationRepository: ConfigurationRepository,
    lifecycleScope: CoroutineScope,
    ledbarId: String,
    ledbarMacAddress: String,
    ledbarName: String,
    configuration: String,
    connectionStatus: ConnectionStatus
) {
    val visibilityMap: SnapshotStateMap<Int, Boolean> = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            for (i in 0 until 8) this[i] = true
        }
    }
    var displayMoments by remember { mutableStateOf(true) }
    var displayEffects by remember { mutableStateOf(true) }
    var loopCount by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf(connectionStatus) }
    val bluetoothManager = BluetoothManagerSingleton.getInstance(context)
    var rgbColorStr by remember { mutableStateOf("255 255 255") }
    var selectedMomentIndex by remember { mutableStateOf(0) }

    var showMenu by remember { mutableStateOf(false) }
    var selectedGroupIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val configurationState = remember {
        mutableStateOf(Json.decodeFromString<Configuration>(configuration))
    }

    // Extract moments from the Configuration object
    val moments = configurationState.value.moments

    // Flatten all color configs to get initial colors per group index
    val initialGroupColors = moments
        .flatMap { it.colorConfig }
        .distinctBy { it.index }
        .associate { it.index to it.rgb }
        .toMutableMap()

    // Flatten effects, indexed by LED group index (customize if needed)
    val initialGroupEffects = mutableMapOf<Int, Effect>()

    moments.flatMap { it.effects }.forEach { effect ->
        when (effect) {
            is Effect.Blink -> effect.affectedGroups.forEach { groupIndex ->
                initialGroupEffects[groupIndex] = effect
            }
            is Effect.Wave -> effect.affectedGroups.forEach { groupIndex ->
                initialGroupEffects[groupIndex] = effect
            }
            Effect.NONE -> {}
        }
    }

    var groupColors by remember { mutableStateOf<Map<Int,String>>(emptyMap()) }
    var groupEffects by remember { mutableStateOf<Map<Int,Effect>>(emptyMap()) }

    LaunchedEffect(moments, displayMoments) {
        if (moments.isEmpty() || !displayMoments) return@LaunchedEffect

        while (true) {
            for (moment in moments) {
                repeat(moment.repeat) {
                    groupColors = moment.colorConfig.associate { it.index to it.rgb }

                    val blinkEffects = moment.effects.filterIsInstance<Effect.Blink>()

                    (0 until 8).forEach { visibilityMap[it] = true }

                    val blinkCount = blinkEffects.maxOfOrNull { it.times } ?: 0
                    val blinkDelay = blinkEffects.firstOrNull()?.delay?.toLong() ?: 0L

                    if (blinkEffects.isNotEmpty()) {
                        for (i in 1..blinkCount) {
                            blinkEffects.forEach { effect ->
                                effect.affectedGroups.forEach { idx -> visibilityMap[idx] = false }
                            }
                            delay(blinkDelay)
                            blinkEffects.forEach { effect ->
                                effect.affectedGroups.forEach { idx -> visibilityMap[idx] = true }
                            }
                            delay(blinkDelay)
                        }
                    }

                    val totalBlinkTime = blinkCount * blinkDelay * 2
                    val remainingDelay = moment.delayMs - totalBlinkTime
                    if (remainingDelay > 0) delay(remainingDelay)
                }
            }
        }
    }





    if (!bluetoothManager.isBluetoothEnabled()) {
        Log.e("PROJEKTAS", "Bluetooth is not enabled. Please enable Bluetooth.")
    }

    BackHandler {
        sendBluetoothCommand(context, "DISCONNECTED\n") {
            navController.navigate(
                Screen.LedbarScreen.withArgs(
                    sharedPreferences.readString("access_code").toString()
                )
            )
            bluetoothManager.closeBluetoothSocket()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 4.dp)
        ) {
            IconButton(
                onClick = {
                    sendBluetoothCommand(context, "DISCONNECTED\n") {
                        navController.navigate(
                            Screen.LedbarScreen.withArgs(
                                sharedPreferences.readString("access_code").toString()
                            )
                        )
                        bluetoothManager.closeBluetoothSocket()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.back_svgrepo_com),
                    contentDescription = "Back button",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 75.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LedbarPreview(
                configuration = configurationState.value,
                groupColors = groupColors,
                groupEffects = groupEffects,
                selectedGroupIndices = selectedGroupIndices,
                onGroupSelected = { newSelection ->
                    selectedGroupIndices = newSelection
                },
                rgbColorStr = rgbColorStr,
                displayMoments = displayMoments,
                displayEffects = displayEffects,
                onToggleMoments = { displayMoments = !displayMoments },
                onToggleEffects = { displayEffects = !displayEffects },
                loopCount,
                selectedMomentIndex,
                visibilityMap
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp, end = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Momentai", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Momentai(configurationState,
                    selectedIndex = selectedMomentIndex,
                    onSelectedIndex = { selectedMomentIndex = it })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Spalva: ")
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(R.drawable.rgb),
                        modifier = Modifier.size(24.dp),
                        contentDescription = "RGB",
                        tint = Color.Unspecified
                    )
                }
            }

            BlinkEffect()

            MoveEffect()

            WaveEffect()

            PulseEffect()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        lifecycleScope.launch {
                            // Example save logic (you'll want to convert groupColors and groupEffects to Moments etc)
                            // configurationRepository.update(/* ... */)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199)),
                ) {
                    Text("IŠSAUGOTI")
                }
            }

            Spacer(modifier = Modifier.size(300.dp))
        }
    }

    if (showMenu) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { showMenu = false },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 6.dp, end = 6.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .padding(top = 20.dp, bottom = 20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            LedbarPreview(
                                configuration = configurationState.value,
                                groupColors = groupColors,
                                groupEffects = groupEffects,
                                selectedGroupIndices = selectedGroupIndices,
                                onGroupSelected = { newSelection -> selectedGroupIndices = newSelection },
                                rgbColorStr = rgbColorStr,
                                displayMoments = displayMoments,
                                displayEffects = displayEffects,
                                onToggleMoments = { displayMoments = !displayMoments },
                                onToggleEffects = { displayEffects = !displayEffects },
                                loopCount,
                                selectedMomentIndex,
                                visibilityMap
                            )

                            RGBColorPicker(
                                context,
                                rgbColorStr,
                                selectedGroupIndices
                            ) { newColorStr ->
                                groupColors = groupColors.toMutableMap().apply {
                                    selectedGroupIndices.forEach { groupIndex -> this[groupIndex] = newColorStr }
                                }
                                rgbColorStr = newColorStr
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { showMenu = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199))
                            ) {
                                Text("IŠSAUGOTI")
                            }
                        }
                    }
                }
            }
        }
    }
}
