package me.frostingly.app.components.data

sealed class Effect() {
    data class Blink(val delay: Int, val times: Int) : Effect()
    data class Wave(val delay: Int, val speed: Int, val times: Int) : Effect()
    object NONE: Effect()
}
