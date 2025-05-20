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
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.Job
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
import me.frostingly.app.components.data.Moment
import me.frostingly.app.components.reorganizeIndices
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

    val currentMove: Effect.Move? =
        configurationState.value
            .moments
            .getOrNull(selectedMomentIndex)
            ?.effects
            ?.filterIsInstance<Effect.Move>()
            ?.firstOrNull()

    val moveEnabled by remember(currentMove, selectedGroupIndices) {
        derivedStateOf {
            currentMove?.affectedGroups
                ?.any { it in selectedGroupIndices }
                ?: false
        }
    }
    var moveSpeed by remember(selectedMomentIndex) {
        mutableStateOf(currentMove?.speed?.toString() ?: "")
    }
    var moveTimes by remember(selectedMomentIndex) {
        mutableStateOf(currentMove?.times?.toString() ?: "")
    }

    val currentBlink: Effect.Blink? =
        configurationState.value
            .moments
            .getOrNull(selectedMomentIndex)
            ?.effects
            ?.filterIsInstance<Effect.Blink>()
            ?.firstOrNull()

    val blinkEnabled by remember(currentBlink, selectedGroupIndices) {
        derivedStateOf {
            currentBlink?.affectedGroups
                ?.any { it in selectedGroupIndices }
                ?: false
        }
    }
    var blinkDelay by remember(selectedMomentIndex) {
        mutableStateOf(currentBlink?.delay?.toString() ?: "")
    }
    var blinkTimes by remember(selectedMomentIndex) {
        mutableStateOf(currentBlink?.times?.toString() ?: "")
    }

    // Extract moments from the Configuration object
    val moments = configurationState.value.moments

    var groupColors by remember { mutableStateOf<Map<Int,String>>(emptyMap()) }
    var groupEffects by remember { mutableStateOf<Map<Int,Effect>>(emptyMap()) }

    val moment = configurationState.value.moments[selectedMomentIndex]

    val activeKinds = moment.effects
        .filter { effect ->
            when (effect) {
                is Effect.Blink    -> selectedGroupIndices.any { it in effect.affectedGroups }
                is Effect.Move     -> selectedGroupIndices.any { it in effect.affectedGroups }
                is Effect.Wave     -> selectedGroupIndices.any { it in effect.affectedGroups }
                else               -> false
            }
        }
        .mapNotNull {
            when (it) {
                is Effect.Blink -> "blink"
                is Effect.Move  -> "move"
                is Effect.Wave  -> "wave"
                else            -> null
            }
        }
        .toSet()

    val allowToggles = activeKinds.size == 1


    LaunchedEffect(moments, displayMoments, displayEffects, selectedMomentIndex) {
        if (moments.isEmpty()) return@LaunchedEffect

        var blinkJob: Job? = null

        while (true) {
            when {
                displayMoments && displayEffects -> {
                    for (moment in moments) {
                        repeat(moment.repeat) {
                            blinkJob?.cancel() // cancel previous blink job if still running

                            groupColors = moment.colorConfig.associate { it.index to it.rgb }

                            val blinkList = moment.effects.filterIsInstance<Effect.Blink>()
                            val blinkCount = blinkList.maxOfOrNull { it.times } ?: 0
                            val blinkDelay = blinkList.firstOrNull()?.delay?.toLong() ?: 0L

                            (0 until 8).forEach { visibilityMap[it] = true }

                            blinkJob = launch {
                                repeat(blinkCount) {
                                    blinkList.forEach { e -> e.affectedGroups.forEach { idx -> visibilityMap[idx] = false } }
                                    delay(blinkDelay)
                                    blinkList.forEach { e -> e.affectedGroups.forEach { idx -> visibilityMap[idx] = true } }
                                    delay(blinkDelay)
                                }
                            }

                            delay(moment.delayMs.toLong())
                        }
                    }
                }

                !displayMoments && displayEffects -> {
                    val m = moments.getOrNull(selectedMomentIndex) ?: break
                    blinkJob?.cancel()

                    groupColors = m.colorConfig.associate { it.index to it.rgb }

                    val blinkList = m.effects.filterIsInstance<Effect.Blink>()
                    val blinkCount = blinkList.maxOfOrNull { it.times } ?: 0
                    val blinkDelay = blinkList.firstOrNull()?.delay?.toLong() ?: 0L

                    (0 until 8).forEach { visibilityMap[it] = true }

                    blinkJob = launch {
                        repeat(blinkCount) {
                            blinkList.forEach { e -> e.affectedGroups.forEach { idx -> visibilityMap[idx] = false } }
                            delay(blinkDelay)
                            blinkList.forEach { e -> e.affectedGroups.forEach { idx -> visibilityMap[idx] = true } }
                            delay(blinkDelay)
                        }
                    }

                    delay(m.delayMs.toLong())
                }

                !displayMoments && !displayEffects -> {
                    val m = moments.getOrNull(selectedMomentIndex) ?: break
                    blinkJob?.cancel()
                    groupColors = m.colorConfig.associate { it.index to it.rgb }
                    delay(m.delayMs.toLong())
                }

                displayMoments && !displayEffects -> {
                    for (moment in moments) {
                        repeat(moment.repeat) {
                            blinkJob?.cancel()
                            groupColors = moment.colorConfig.associate { it.index to it.rgb }
                            delay(moment.delayMs.toLong())
                        }
                    }
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

            BlinkEffect(
                enabled      = allowToggles && "blink" in activeKinds,
                delayMs      = blinkDelay,
                times        = blinkTimes,
                onEnabledChange = { checked ->
                    val m = configurationState.value.moments[selectedMomentIndex]
                    val oldBlinks = m.effects.filterIsInstance<Effect.Blink>()
                    val newBlink = if (checked) {
                        // union of existing and newly selected groups
                        Effect.Blink(
                            affectedGroups = (oldBlinks.flatMap { it.affectedGroups } + selectedGroupIndices)
                                .distinct(),
                            delay = blinkDelay.toIntOrNull() ?: 0,
                            times = blinkTimes.toIntOrNull() ?: 0
                        )
                    } else {
                        null
                    }

                    configurationState.value = configurationState.value.copy(
                        moments = configurationState.value.moments.mapIndexed { idx, mm ->
                            if (idx == selectedMomentIndex) {
                                val others = mm.effects.filterNot { it is Effect.Blink }
                                if (newBlink != null) mm.copy(effects = others + newBlink)
                                else mm.copy(effects = others)
                            } else mm
                        }
                    )
                },
                onDelayChange = { newDelay ->
                    blinkDelay = newDelay
                },
                onTimesChange = { newTimes ->
                    blinkTimes = newTimes
                },
                onSave = {
                    val delayInt = blinkDelay.toIntOrNull()
                    if (delayInt == null) {
                        Log.d("PROJEKTAS", "Invalid delay: $blinkDelay")
                        return@BlinkEffect
                    }
                    val timesInt = blinkTimes.toIntOrNull()
                    if (timesInt == null) {
                        Log.d("PROJEKTAS", "Invalid times: $blinkTimes")
                        return@BlinkEffect
                    }

                    configurationState.value = configurationState.value.copy(
                        moments = configurationState.value.moments.mapIndexed { idx, m ->
                            if (idx == selectedMomentIndex) {
                                val others = m.effects.filterNot { it is Effect.Blink }
                                m.copy(effects = others + Effect.Blink(
                                    affectedGroups = selectedGroupIndices.toList(),
                                    delay = delayInt,
                                    times = timesInt
                                ))
                            } else m
                        }
                    )
                }
            )

            MoveEffect(
                enabled      = allowToggles && "move" in activeKinds,
                speedMs      = moveSpeed,
                times        = moveTimes,
                onEnabledChange = { checked ->
                    val m = configurationState.value.moments[selectedMomentIndex]
                    val oldMoves = m.effects.filterIsInstance<Effect.Move>()
                    val newMove = if (checked) {
                        // union of existing and newly selected groups
                        Effect.Move(
                            affectedGroups = (oldMoves.flatMap { it.affectedGroups } + selectedGroupIndices)
                                .distinct(),
                            speed = moveSpeed.toIntOrNull() ?: 0,
                            times = moveTimes.toIntOrNull() ?: 0
                        )
                    } else {
                        null
                    }

                    configurationState.value = configurationState.value.copy(
                        moments = configurationState.value.moments.mapIndexed { idx, mm ->
                            if (idx == selectedMomentIndex) {
                                val others = mm.effects.filterNot { it is Effect.Move }
                                if (newMove != null) mm.copy(effects = others + newMove)
                                else mm.copy(effects = others)
                            } else mm
                        }
                    )
                },
                onSpeedChange = { newSpeed ->
                    moveSpeed = newSpeed
                    Log.d("PROJEKTAS", moveSpeed)
                },
                onTimesChange = { newTimes ->
                    moveTimes = newTimes
                    Log.d("PROJEKTAS", moveTimes)
                },
                onSave = {
                    val speedInt = moveSpeed.toIntOrNull()
                    if (speedInt == null) {
                        Log.d("PROJEKTAS", "Invalid speed: $moveSpeed")
                        return@MoveEffect
                    }
                    val timesInt = moveTimes.toIntOrNull()
                    if (timesInt == null) {
                        Log.d("PROJEKTAS", "Invalid times: $moveTimes")
                        return@MoveEffect
                    }

                    Log.d("PROJEKTAS", "Saving Move: speed=$speedInt times=$timesInt")

                    configurationState.value = configurationState.value.copy(
                        moments = configurationState.value.moments.mapIndexed { idx, m ->
                            if (idx == selectedMomentIndex) {
                                val others = m.effects.filterNot { it is Effect.Move }
                                m.copy(effects = others + Effect.Move(
                                    affectedGroups = selectedGroupIndices.toList(),
                                    speed = speedInt,
                                    times = timesInt
                                )).also { updatedMoment ->
                                    Log.d("PROJEKTAS", "Updated moment $idx effects: ${updatedMoment.effects}")
                                }
                            } else m
                        }
                    )
                }
            )

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
                            configurationState.value.moments.forEach { moment ->
                                val mi = moment.id - 1

                                // 1) Activate moment
                                sendBluetoothCommand(context, "am($mi)\n") {}
                                delay(200L)

                                // 2) Set moment timing
                                sendBluetoothCommand(
                                    context,
                                    "smi($mi,${moment.delayMs},${moment.repeat})\n"
                                ) {}
                                delay(200L)

                                // 3) Send all gc(...) for colorConfigs
                                moment.colorConfig.forEach { cc ->
                                    val gs = reorganizeIndices(setOf(cc.index))
                                    sendBluetoothCommand(
                                        context,
                                        "gc($mi,[$gs], \"${cc.rgb}\")\n"
                                    ) {}
                                    delay(200L)
                                }

                                // 4) Combine and send MOVE effect(s)
// 4) Send MOVE effect
                                val move = moment.effects.filterIsInstance<Effect.Move>().firstOrNull()
                                if (move != null) {
                                    // move.affectedGroups already contains the full group set
                                    val grpStr = reorganizeIndices(move.affectedGroups.toSet())

                                    val times = move.times
                                    val speed = move.speed

                                    // Note: Arduino expects MoveEffect(times, timeDelay, …)
                                    val cmd = "ge($mi,[$grpStr], \"M\"($times,$speed))\n"
                                    Log.d("PROJEKTAS", "Sending MOVE command: $cmd")
                                    sendBluetoothCommand(context, cmd) {}
                                    delay(200L)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(11, 77, 199))
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
                                configurationState.value = configurationState.value.copy(
                                    moments = configurationState.value.moments.mapIndexed { idx, moment ->
                                        if (idx == selectedMomentIndex) {
                                            val updatedColorConfigs = moment.colorConfig.map { cc ->
                                                if (cc.index in selectedGroupIndices) {
                                                    cc.copy(rgb = newColorStr)
                                                } else cc
                                            }
                                            moment.copy(colorConfig = updatedColorConfigs)
                                        } else moment
                                    }
                                )

                                groupColors = groupColors.toMutableMap().apply {
                                    selectedGroupIndices.forEach { groupIndex ->
                                        this[groupIndex] = newColorStr
                                    }
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