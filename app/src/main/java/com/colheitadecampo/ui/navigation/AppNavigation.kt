package com.colheitadecampo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.colheitadecampo.ui.screens.dashboard.DashboardScreen
import com.colheitadecampo.ui.screens.harvest.HarvestScreen
import com.colheitadecampo.ui.screens.home.HomeScreen
import com.colheitadecampo.ui.screens.importexport.ImportExportScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val actions = remember(navController) { NavigationActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navigateToImportExport = actions.navigateToImportExport,
                navigateToDashboard = actions.navigateToDashboard,
                navigateToHarvest = actions.navigateToHarvest
            )
        }
        
        composable(Screen.ImportExport.route) {
            ImportExportScreen(
                navigateUp = actions.navigateUp
            )
        }
        
        composable(
            route = "${Screen.Dashboard.route}/{fieldId}",
            arguments = listOf(
                navArgument("fieldId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val fieldId = backStackEntry.arguments?.getLong("fieldId") ?: 0L
            DashboardScreen(
                fieldId = fieldId,
                navigateUp = actions.navigateUp,
                navigateToHarvest = { actions.navigateToHarvestWithField(fieldId) }
            )
        }
        
        composable(
            route = "${Screen.Harvest.route}/{fieldId}",
            arguments = listOf(
                navArgument("fieldId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val fieldId = backStackEntry.arguments?.getLong("fieldId") ?: 0L
            HarvestScreen(
                fieldId = fieldId,
                navigateUp = actions.navigateUp
            )
        }
    }
}

class NavigationActions(private val navController: NavHostController) {
    
    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    
    val navigateToImportExport: () -> Unit = {
        navController.navigate(Screen.ImportExport.route)
    }
    
    val navigateToDashboard: (Long) -> Unit = { fieldId ->
        navController.navigate(Screen.Dashboard.withArgs(fieldId.toString()))
    }
    
    val navigateToHarvest: (Long) -> Unit = { fieldId ->
        navController.navigate(Screen.Harvest.withArgs(fieldId.toString()))
    }
    
    val navigateToHarvestWithField: (Long) -> Unit = { fieldId ->
        navController.navigate(Screen.Harvest.withArgs(fieldId.toString())) {
            popUpTo(Screen.Dashboard.route) { inclusive = false }
        }
    }
    
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }
}
