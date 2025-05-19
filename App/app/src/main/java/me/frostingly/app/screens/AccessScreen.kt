package me.frostingly.app.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.frostingly.app.R
import me.frostingly.app.Screen
import me.frostingly.app.SharedPreferences
import me.frostingly.app.components.data.ColorConfig
import me.frostingly.app.components.data.Effect
import me.frostingly.app.components.data.Moment
import me.frostingly.app.room.ConfigurationDB.Configuration
import me.frostingly.app.room.ConfigurationDB.ConfigurationRepository
import me.frostingly.app.room.LedbarDB.Ledbar
import me.frostingly.app.room.LedbarDB.LedbarRepository

@Composable
fun AccessScreen(navController: NavController, sharedPreferences: SharedPreferences, context: Context,
                 ledbarRepository: LedbarRepository,
                 configurationRepository: ConfigurationRepository,
                 lifecycleScope: CoroutineScope
) {
    var text by remember { mutableStateOf("") }
    val maxChars = 6

    LaunchedEffect(Unit) {
        ledbarRepository.clearAllLedbars()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painterResource(id = R.drawable.logotipas),
                "Logotipas",
                modifier = Modifier.padding(30.dp)
            )

            Text(
                text = "Įveskite prieigos kodą:",
                fontSize = 30.sp,
                modifier = Modifier.padding(15.dp)
            )
            Box(
                modifier = Modifier
                    .border(2.dp, Color(11, 77, 199), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .width(240.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = {
                        if (it.length <= maxChars) text = it
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 30.sp, color = Color.Black),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Prieigos kodas",
                                color = Color.Gray,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Button(
                contentPadding = PaddingValues(20.dp),
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 10.dp)
                    .width(200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(11, 77, 199)
                ),
                onClick = {
                    if (text == "WW2025") {
                        val config1 = Configuration("1", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "255,0,0"),
                                ColorConfig(1, "255,0,0"),
                                ColorConfig(2, "255,0,0"),
                                ColorConfig(3, "0,255,0"),
                                ColorConfig(4, "0,0,255"),
                                ColorConfig(5, "0,0,255"),
                                ColorConfig(7, "0,0,255"),
                            ), listOf()
                        )))
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
                                Effect.Blink(listOf(0, 1, 2, 3, 4, 5, 6, 7), 500, 3)
                            )
                        )))
                        val config3 = Configuration("3", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "255,165,0"),
                                ColorConfig(1, "255,165,0"),
                                ColorConfig(2, "255,165,0"),
                                ColorConfig(3, "255,165,0"),
                                ColorConfig(4, "255,165,0"),
                                ColorConfig(5, "255,165,0"),
                                ColorConfig(6, "255,165,0"),
                                ColorConfig(7, "255,165,0"),
                            ), listOf()
                        )))
                        val config4 = Configuration("4", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "128,0,128"),
                                ColorConfig(1, "255,192,203"),
                                ColorConfig(2, "128,0,128"),
                                ColorConfig(3, "255,192,203"),
                                ColorConfig(4, "128,0,128"),
                                ColorConfig(5, "255,192,203"),
                                ColorConfig(6, "128,0,128"),
                                ColorConfig(7, "255,192,203"),
                            ), listOf()
                        )))
                        val config5 = Configuration("5", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "0,255,255"),
                                ColorConfig(1, "0,255,255"),
                                ColorConfig(2, "0,255,255"),
                                ColorConfig(3, "0,255,255"),
                                ColorConfig(4, "255,105,180"),
                                ColorConfig(5, "255,105,180"),
                                ColorConfig(6, "255,105,180"),
                                ColorConfig(7, "255,105,180"),
                            ), listOf()
                        )))
                        val config6 = Configuration("6", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "0,0,0"),
                                ColorConfig(1, "0,0,0"),
                                ColorConfig(2, "0,0,0"),
                                ColorConfig(3, "0,0,0"),
                                ColorConfig(4, "0,0,0"),
                                ColorConfig(5, "0,0,0"),
                                ColorConfig(6, "0,0,0"),
                                ColorConfig(7, "0,0,0"),
                            ), listOf()
                        )))
                        val config7 = Configuration("7", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "255,255,255"),
                                ColorConfig(1, "255,255,255"),
                                ColorConfig(2, "255,255,255"),
                                ColorConfig(3, "255,255,255"),
                                ColorConfig(4, "255,255,255"),
                                ColorConfig(5, "255,255,255"),
                                ColorConfig(6, "255,255,255"),
                                ColorConfig(7, "255,255,255"),
                            ), listOf()
                        )))
                        val config8 = Configuration("8", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "75,0,130"),
                                ColorConfig(1, "75,0,130"),
                                ColorConfig(2, "75,0,130"),
                                ColorConfig(3, "75,0,130"),
                                ColorConfig(4, "238,130,238"),
                                ColorConfig(5, "238,130,238"),
                                ColorConfig(6, "238,130,238"),
                                ColorConfig(7, "238,130,238"),
                            ), listOf()
                        )))
                        val config9 = Configuration("9", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "0,128,0"),
                                ColorConfig(1, "0,0,128"),
                                ColorConfig(2, "0,128,0"),
                                ColorConfig(3, "0,0,128"),
                                ColorConfig(4, "0,128,0"),
                                ColorConfig(5, "0,0,128"),
                                ColorConfig(6, "0,128,0"),
                                ColorConfig(7, "0,0,128"),
                            ), listOf()
                        )))
                        val config10 = Configuration("10", moments = listOf(Moment(
                            1, 1000, 3, listOf(
                                ColorConfig(0, "255,69,0"),
                                ColorConfig(1, "255,69,0"),
                                ColorConfig(2, "0,255,127"),
                                ColorConfig(3, "0,255,127"),
                                ColorConfig(4, "70,130,180"),
                                ColorConfig(5, "70,130,180"),
                                ColorConfig(6, "199,21,133"),
                                ColorConfig(7, "199,21,133"),
                            ), listOf()
                        )))

                        lifecycleScope.launch {
                            val ledbars = listOf(
                                Ledbar(
                                    "šviestuvas1",
                                    "00:00:13:00:09:3D",
                                    "Pirmas šviestuvas",
                                    2,
                                    config1
                                ),
                                Ledbar(
                                    "šviestuvas2",
                                    "00:00:13:00:0B:D3",
                                    "Antras šviestuvas",
                                    2,
                                    config2
                                ),
                                Ledbar(
                                    "šviestuvas3",
                                    "00:00:13:00:0C:AA",
                                    "Trečias šviestuvas",
                                    2,
                                    config3
                                ),
                                Ledbar(
                                    "šviestuvas4",
                                    "00:00:13:00:0D:BB",
                                    "Ketvirtas šviestuvas",
                                    2,
                                    config4
                                ),
                                Ledbar(
                                    "šviestuvas5",
                                    "00:00:13:00:0E:CC",
                                    "Penktas šviestuvas",
                                    3,
                                    config5
                                ),
                                Ledbar(
                                    "šviestuvas6",
                                    "00:00:13:00:0F:DD",
                                    "Šeštas šviestuvas",
                                    3,
                                    config6
                                ),
                                Ledbar(
                                    "šviestuvas7",
                                    "00:00:13:00:10:EE",
                                    "Septintas šviestuvas",
                                    3,
                                    config7
                                ),
                                Ledbar(
                                    "šviestuvas8",
                                    "00:00:13:00:11:FF",
                                    "Aštuntas šviestuvas",
                                    3,
                                    config8
                                ),
                                Ledbar(
                                    "šviestuvas9",
                                    "00:00:13:00:12:AB",
                                    "Devintas šviestuvas",
                                    3,
                                    config9
                                ),
                                Ledbar(
                                    "šviestuvas10",
                                    "00:00:13:00:13:BC",
                                    "Dešimtas šviestuvas",
                                    3,
                                    config10
                                )
                            )

                            ledbars.forEach { ledbar ->
                                if (ledbarRepository.getLedbarById(ledbar.id) == null) {
                                    ledbarRepository.insertLedbar(ledbar)
                                } else {
                                    ledbarRepository.updateLedbar(ledbar)
                                }
                            }

                            navController.navigate(Screen.LedbarScreen.withArgs(text))
                            sharedPreferences.writeString("first_time", "false")
                            sharedPreferences.writeString("access_code", text)
                        }
                    }
                }
            ) {
                Text(
                    text = "Patvirtinti",
                    fontSize = 25.sp,
                )
            }

            val annotatedText = buildAnnotatedString {
                append("arba naudokite svečio prieigą paspaudę ")
                val start = length
                append("čia")
                addStyle(
                    style = SpanStyle(color = Color.Blue),
                    start = start,
                    end = length
                )
                addStringAnnotation(
                    tag = "JETPACK_TAG",
                    annotation = "jetpack_compose",
                    start = start,
                    end = length
                )
            }

            ClickableText(modifier = Modifier
                .padding(bottom = 80.dp),
                text = annotatedText,
                onClick = { offset ->
                    val annotations = annotatedText.getStringAnnotations(
                        tag = "JETPACK_TAG",
                        start = offset,
                        end = offset
                    )
                    if (annotations.isNotEmpty()) {

                        navController.navigate(Screen.LedbarScreen.withArgs("GUEST"))
                        sharedPreferences.writeString("first_time", "false")
                        sharedPreferences.writeString("access_code", "GUEST")
                    }
                }
            )
        }
    }
}