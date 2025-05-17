package me.frostingly.app.screens
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import me.frostingly.app.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import me.frostingly.app.Screen
import me.frostingly.app.SharedPreferences
import me.frostingly.app.bluetooth.BluetoothManagerSingleton
import me.frostingly.app.bluetooth.ConnectionStatus
import me.frostingly.app.components.Preview.LedbarPreviewUI
import me.frostingly.app.room.ConfigurationDB.ConfigurationRepository
import me.frostingly.app.room.LedbarDB.Ledbar
import me.frostingly.app.room.LedbarDB.LedbarRepository
import sendBluetoothCommand

@Composable
fun LedbarScreen(
    navController: NavController,
    access_code: String?,
    sharedPreferences: SharedPreferences,
    context: Context,
    ledbarRepository: LedbarRepository,
    configurationRepository: ConfigurationRepository,
    lifecycleScope: CoroutineScope
) {
    val bluetoothManager = BluetoothManagerSingleton.getInstance(context)
    var ledbars by remember { mutableStateOf<List<Ledbar>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var connectionStatus by remember { mutableStateOf(ConnectionStatus.IDLE) }

    LaunchedEffect(Unit) {
        isLoading = true
        ledbars = ledbarRepository.getAllLedbars()
        isLoading = false
    }

    Box(
        modifier = Modifier.fillMaxSize() // Make Box fill the screen
    ) {
        // Main content that will scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
        ) {
            if (connectionStatus == ConnectionStatus.CONNECTING) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text("Connecting to ledbar... (${bluetoothManager.getCurrentRetryCount()}/3)", fontSize = 20.sp)
                    }
                }
            } else if (connectionStatus == ConnectionStatus.IDLE) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text("Loading devices...", fontSize = 20.sp)
                        }
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.hamburger),
                        contentDescription = "Menu Icon",
                        modifier = Modifier
                            .wrapContentSize()
                            .size(48.dp)
                            .padding(start=5.dp)
                            .clickable {
                                sharedPreferences.writeString("first_time", "true")
                                sharedPreferences.writeString("access_code", "null")
                                navController.navigate(Screen.AccessScreen.route)
                            }
                    )
                    if (ledbars.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nėra šviestuvų, gal reiktų sukurti vieną?", fontSize = 20.sp, color = Color.Red)
                        }
                    } else {
                        // You can add your other UI content here, like the LazyColumn, Menu icon, etc.
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f) // Allow scrolling
                                .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                            contentPadding = PaddingValues(bottom = 50.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item {
                                Text(
                                    text = "Šviestuvai (" + ledbars.size + ")",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            items(ledbars) { ledbar ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(125.dp)
                                        .shadow(1.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)) // Round only the bottom corners
//                                        .border(1.dp, color = Color.LightGray, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                ) {
                                    LedbarPreviewUI(ledbar.configuration)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 5.dp, end = 10.dp, top = 50.dp)
                                    ) {
                                        Text(
                                            text = ledbar.name,
                                            color = Color.Black,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 5.dp, end = 10.dp, top = 85.dp)
                                    ) {
                                        Text(
                                            text = "Aukštas: " + ledbar.aukstas,
                                            modifier = Modifier.padding(top = 5.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 5.dp, end = 10.dp, top = 100.dp)
                                    ) {
                                        Text(
                                            text = "ID: " + ledbar.id,
                                            modifier = Modifier.padding(top = 5.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 125.dp, end = 10.dp, top = 70.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Koreguoti Button
                                        Button(
                                            modifier = Modifier
                                                .height(40.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(11, 77, 199)
                                            ),
                                            shape = RoundedCornerShape(36.dp),
                                            onClick = {
                                                connectionStatus = ConnectionStatus.CONNECTING

                                                bluetoothManager.connectToDevice(
                                                    macAddress = ledbar.mac_address,
                                                    onConnected = {
                                                        connectionStatus = ConnectionStatus.CONNECTED
                                                        sendBluetoothCommand(context, "CONNECTED\n") {}

                                                        navController.navigate(
                                                            Screen.ControlScreen.withArgs(
                                                                sharedPreferences.readString("access_code")
                                                                    .toString(),
                                                                ledbar.id,
                                                                ledbar.mac_address,
                                                                ledbar.name,
                                                                ledbar.configuration,
                                                                connectionStatus.toString()
                                                            )
                                                        )
                                                    },
                                                    onError = { error ->
                                                        connectionStatus = ConnectionStatus.ERROR
                                                        Log.e("PROJEKTAS", "Error: $error")
                                                    }
                                                )
                                            }
                                        ) {
                                            Text(text = "Koreguoti", fontSize = 14.sp)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .height(40.dp)
                                                .width(200.dp)
                                                .clip(RoundedCornerShape(72.dp))
                                                .background(Color(11, 77, 199))
                                        ) {
                                            // Siųsti button
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .padding(start = 12.5.dp)
                                                    .clickable {
                                                        Log.d("PROJEKTAS", "siusti CLICKED!") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Siųsti", color = Color.White, fontSize = 14.sp)
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Divider
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .fillMaxHeight()
                                                    .background(Color.White.copy(alpha = 0.4f))
                                            )

                                            // Dropdown icon box with .weight(1f)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable {
                                                        Log.d("PROJEKTAS", "DROPDOWN CLICKED!")
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.dropdown),
                                                    contentDescription = "Dropdown",
                                                    modifier = Modifier.size(30.dp).padding(end = 5.dp),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if ((!isLoading && connectionStatus != ConnectionStatus.CONNECTING) && (!isLoading && connectionStatus != ConnectionStatus.CONNECTED)) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(34, 139, 34)
                ),
                onClick = {
                    // Handle button click
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)  // Position the button at the bottom-right corner
                    .padding(bottom = 72.dp, end = 24.dp) // Optional padding
            ) {
                Text("Pridėti naują")
            }
        }
    }
}

