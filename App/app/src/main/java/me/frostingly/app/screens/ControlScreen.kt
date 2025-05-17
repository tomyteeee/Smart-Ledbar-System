package me.frostingly.app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import me.frostingly.app.R
import me.frostingly.app.Screen
import me.frostingly.app.SharedPreferences
import me.frostingly.app.bluetooth.BluetoothManagerSingleton
import me.frostingly.app.bluetooth.ConnectionStatus
import me.frostingly.app.components.Checkboxes.BlinkEffect
import me.frostingly.app.components.data.Effect
import me.frostingly.app.components.Preview.LedbarPreview
import me.frostingly.app.components.Momentai
import me.frostingly.app.components.Checkboxes.MoveEffect
import me.frostingly.app.components.Checkboxes.PulseEffect
import me.frostingly.app.components.RGBColorPicker
import me.frostingly.app.components.Checkboxes.WaveEffect
import me.frostingly.app.components.Preview.colorToRgbString
import me.frostingly.app.components.Preview.parseColors
import me.frostingly.app.components.Preview.parseEffects
import me.frostingly.app.components.Preview.parseMoments
import me.frostingly.app.room.ConfigurationDB.ConfigurationRepository
import me.frostingly.app.room.LedbarDB.LedbarRepository
import sendBluetoothCommand

//00:00:13:00:09:3D

/*
* - spalvų savaitė (kiekvina diena skirtinga spalva)
- Per šventes pagal tinkamiausią spalvą
- galaktika
- prieš skambutį pradeda mirksėti šviesa
- pertrauka - žalia, 2 min iki skambučio geltona, nuskambėjus skambučiui į pamoką raudona
- per petraukas bėgančios spalvos (2 arba3)
* */



@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@SuppressLint("MissingPermission")
@Composable
fun ControlScreen(navController: NavController, access_code: String, sharedPreferences: SharedPreferences,
                  context: Context, ledbarRepository: LedbarRepository,
                  configurationRepository: ConfigurationRepository,
                  lifecycleScope: CoroutineScope,
                  ledbarId: String,
                  ledbarMacAddress: String,
                  ledbarName: String,
                  ledbarConfiguration: String,
                  connectionStatus: ConnectionStatus
) {
    var text by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf(connectionStatus) }
    val bluetoothManager = BluetoothManagerSingleton.getInstance(context)
    var rgbColorStr by remember { mutableStateOf("255 255 255") }

    var showMenu by remember { mutableStateOf(false) }
    var selectedGroupIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val momentSection = ledbarConfiguration
        .substringAfter("moments(")
        .substringBeforeLast(")")
        .trim()

    val configWithoutMoments = ledbarConfiguration
        .replace(Regex("""moments\((.*?)\)""", RegexOption.DOT_MATCHES_ALL), "")
        .trim()
        .trim(';', ':')  // clean trailing/leading semicolons or colons

    val colorSection = configWithoutMoments
        .substringBefore("effects=", "")
        .removePrefix(":")
        .trim(';', ':') // clean again if any lingering separators

    val effectSection = configWithoutMoments
        .substringAfter("effects=", "")
        .removePrefix(":")
        .trim(';', ':')
        .let { if (it.isNotEmpty()) "effects=$it" else "" }


    val initialLedColors = parseColors(colorSection, 8)
    val initialLedEffects = parseEffects(effectSection, 8)
    Log.d("PROJEKTAS", "Input to parseMoments: [$momentSection]")
    val initialMoments = parseMoments(momentSection)
    initialMoments.forEachIndexed { index, moment ->
        Log.d("PROJEKTAS", "Moment ${moment.id} params: ${moment.delayMs}, ${moment.repeat}\nColors config: ${moment.colorConfig}\nEffect config: ${moment.effectConfig}\n")
    }
    initialLedEffects.forEachIndexed { index, effect ->
        when (effect) {
            is Effect.Blink -> {
                val delay = effect.delay
                val times = effect.times
                Log.d("PROJEKTAS", "LED Group $index has a blink effect: delay = $delay ms, times = $times")
            }

            is Effect.Wave -> {
                val delay = effect.delay
                val speed = effect.speed
                val times = effect.times
                Log.d("PROJEKTAS", "LED Group $index has a wave effect: delay = $delay ms, speed = $speed ms, times = $times")
            }

            Effect.NONE -> {
                Log.d("PROJEKTAS", "LED Group $index has no effect applied")
            }
        }
    }
    val initialGroupColors = initialLedColors.mapIndexed { index, color ->
        index to colorToRgbString(color)
    }.toMap().toMutableMap()

    val initialGroupEffects = initialLedEffects.mapIndexed { index, effect ->
        index to effect
    }.toMap().toMutableMap()

    Log.d("PROJEKTAS", ledbarConfiguration)
    Log.d("PROJEKTAS", effectSection)
    Log.d("PROJEKTAS", colorSection)

    var groupColors by remember { mutableStateOf(initialGroupColors) }
    var groupEffects by remember { mutableStateOf(initialGroupEffects) }

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
                configuration = ledbarConfiguration,
                groupColors = groupColors,
                groupEffects = groupEffects,
                selectedGroupIndices = selectedGroupIndices,
                onGroupSelected = { newSelection ->
                    selectedGroupIndices = newSelection
                },
                rgbColorStr = rgbColorStr,
                true
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp, end = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Momentai", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Momentai()
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
                    Icon(painter = painterResource(R.drawable.rgb), modifier = Modifier
                        .size(24.dp), contentDescription = "RGB", tint = Color.Unspecified)
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
                Button(onClick = {
                    //save new configuration to the configurations databse and ledbar's data

                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(11, 77, 199)
                    ),
                ) {
                    Text("IŠSAUGOTI")
                }
            }

            Spacer(modifier = Modifier.size(300.dp))
        }

//        Button(onClick = {
//            if (selectedGroupIndices.isNotEmpty()) {
//                sendBluetoothCommand(context, "ge([${reorganizeIndices(selectedGroupIndices)}], \"M(5, 250)\")\n") {}
//            }
//        }) {
//            Text("Move")
//        }
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            LedbarPreview(
                                configuration = ledbarConfiguration,
                                groupColors = groupColors,
                                groupEffects = groupEffects,
                                selectedGroupIndices = selectedGroupIndices,
                                onGroupSelected = { newSelection ->
                                    selectedGroupIndices = newSelection
                                },
                                rgbColorStr = rgbColorStr,
                                false
                            )

                            RGBColorPicker(
                                context,
                                rgbColorStr,
                                selectedGroupIndices
                            ) { newColorStr ->
                                groupColors = groupColors.toMutableMap().apply {
                                    selectedGroupIndices.forEach { groupIndex ->
                                        this[groupIndex] = newColorStr
                                    }
                                }
                                rgbColorStr = newColorStr
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(onClick = { showMenu = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(11, 77, 199)
                                ),
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



