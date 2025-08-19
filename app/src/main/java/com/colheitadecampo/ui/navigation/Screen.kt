package com.colheitadecampo.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ImportExport : Screen("import_export")
    object Dashboard : Screen("dashboard")
    object Harvest : Screen("harvest")
    
    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
