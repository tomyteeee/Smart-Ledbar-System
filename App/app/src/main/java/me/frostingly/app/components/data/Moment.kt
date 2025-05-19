package me.frostingly.app.components.data

import kotlinx.serialization.Serializable

@Serializable
data class Moment(val id: Int,
                  val delayMs: Int,
                  val repeat: Int,
                  val colorConfig: List<ColorConfig>,
                  val effects: List<Effect>
                    )
