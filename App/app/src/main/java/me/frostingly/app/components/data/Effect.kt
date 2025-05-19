package me.frostingly.app.components.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Effect() {
    @Serializable
    data class Blink(val affectedGroups: List<Int> = emptyList(), val delay: Int, val times: Int) : Effect()
    @Serializable
    data class Wave(val affectedGroups: List<Int> = emptyList(), val delay: Int, val speed: Int, val times: Int) : Effect()
    @Serializable
    object NONE: Effect()
}
