package me.frostingly.app

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import me.frostingly.app.bluetooth.ConnectionStatus
import me.frostingly.app.components.data.ColorConfig
import me.frostingly.app.components.data.Effect
import me.frostingly.app.components.data.Moment
import me.frostingly.app.room.ConfigurationDB.Configuration
import me.frostingly.app.room.ConfigurationDB.ConfigurationRepository
import me.frostingly.app.room.LedbarDB.LedbarRepository
import me.frostingly.app.screens.AccessScreen
import me.frostingly.app.screens.ControlScreen
import me.frostingly.app.screens.LedbarScreen

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun Navigation(sharedPreferences: SharedPreferences, context: Context, ledbarRepository: LedbarRepository,
               configurationRepository: ConfigurationRepository,
               lifecycleScope: CoroutineScope) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.AccessScreen.route) {
        composable(
            route = Screen.AccessScreen.route,
        ) {
            if (sharedPreferences.readString("first_time") == null ||
                sharedPreferences.readString("first_time").toBoolean() == true) {
                AccessScreen(
                    navController = navController,
                    sharedPreferences,
                    context,
                    ledbarRepository,
                    configurationRepository,
                    lifecycleScope
                )
            } else {
                //2(3000,1):colors=0-7:255,0,0;)
                val config2 = Configuration("2", moments = listOf(Moment(
                    1, 1000, 3, listOf(
                        ColorConfig(0, "255,255,0"),
                        ColorConfig(1, "255,255,0"),
                        ColorConfig(2, "255,255,0"),
                        ColorConfig(3, "0,255,255"),
                        ColorConfig(4, "0,255,255"),
                        ColorConfig(5, "0,255,255"),
                        ColorConfig(6, "0,255,255"),
                        ColorConfig(7, "0,255,255"),
                    ), listOf(

                    )
                )))
//                AccessScreen(
//                    navController = navController,
//                    sharedPreferences,
//                    context,
//                    ledbarRepository,
//                    configurationRepository,
//                    lifecycleScope
//                )
//                val jsonString = Json.encodeToString(config2)
//                ControlScreen(
//                    navController,
//                    "WW2025",
//                    sharedPreferences,
//                    context,
//                    ledbarRepository,
//                    configurationRepository,
//                    lifecycleScope,
//                    "šviestuvas2",
//                    "00:00:13:00:0B:D3",
//                    "Antras šviestuvas",
//                    jsonString,
//                    ConnectionStatus.valueOf("CONNECTED")
//                )
                LedbarScreen(
                    navController,
                    sharedPreferences.readString("access_code"),
                    sharedPreferences,
                    context,
                    ledbarRepository,
                    configurationRepository,
                    lifecycleScope
                )
            }
        }
        composable(
            route = Screen.LedbarScreen.route + "/{access_code}",
            arguments = listOf(
                navArgument("access_code") {
                    type = NavType.StringType
                },
            )
        ) { entry ->

            LedbarScreen(
                navController,
                access_code = entry.arguments?.getString("access_code"),
                sharedPreferences,
                context,
                ledbarRepository,
                configurationRepository,
                lifecycleScope
            )
        }

        composable(
            route = Screen.ControlScreen.route + "/{access_code}/{id}/{mac_address}/{name}/{configuration}/{connection_status}",
            arguments = listOf(
                navArgument("access_code") {
                    type = NavType.StringType
                },
                navArgument("id") {
                    type = NavType.StringType
                },
                navArgument("mac_address") {
                    type = NavType.StringType
                },
                navArgument("name") {
                    type = NavType.StringType
                },
                navArgument("configuration") {
                    type = NavType.StringType
                },
                navArgument("connection_status") {
                    type = NavType.StringType
                },
            )
        ) { entry ->
            ControlScreen(
                navController,
                entry.arguments?.getString("access_code").toString(),
                sharedPreferences,
                context,
                ledbarRepository,
                configurationRepository,
                lifecycleScope,
                entry.arguments?.getString("id").toString(),
                entry.arguments?.getString("mac_address").toString(),
                entry.arguments?.getString("name").toString(),
                entry.arguments?.getString("configuration").toString(),
                ConnectionStatus.valueOf(entry.arguments?.getString("connection_status").toString())
            )
        }
    }
}