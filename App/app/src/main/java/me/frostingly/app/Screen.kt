package me.frostingly.app

sealed class Screen(val route: String) {
    object AccessScreen : Screen("access_screen")
    object LedbarScreen : Screen("ledbar_screen")
    object ControlScreen : Screen("control_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}