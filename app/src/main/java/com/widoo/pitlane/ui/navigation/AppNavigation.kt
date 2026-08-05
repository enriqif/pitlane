package com.widoo.pitlane.ui.navigation

import android.R.attr.type
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.widoo.pitlane.ui.screen.charts.ChartsScreen
import com.widoo.pitlane.ui.screen.fuel.FuelScreen
import com.widoo.pitlane.ui.screen.home.HomeScreen
import com.widoo.pitlane.ui.screen.profile.VehicleProfileScreen
import com.widoo.pitlane.ui.screen.reminder.ReminderScreen
import com.widoo.pitlane.ui.screen.service.AddServiceScreen
import com.widoo.pitlane.ui.screen.service.ServiceDetailScreen
import com.widoo.pitlane.ui.screen.service.ServiceScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Filled.Home)
    object Service : Screen("service", "Servicios", Icons.Filled.Build)
    object Fuel : Screen("fuel", "Nafta", Icons.Filled.LocalGasStation)
    object Charts : Screen("charts", "Gráficos", Icons.Filled.BarChart)
    object Reminder : Screen("reminder", "Alertas", Icons.Filled.Notifications)
    object Profile : Screen("profile", "Perfil", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Service,
    Screen.Fuel,
    Screen.Charts,
    Screen.Reminder,
    Screen.Profile
)

object Routes {
    const val ADD_SERVICE = "add_service"
    const val SERVICE_DETAIL = "service_detail/{serviceId}"

    fun serviceDetail(serviceId: Long) = "service_detail/$serviceId"
}

@Composable
fun AppNavigation(startDestination: String = Screen.Home.route) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Fuel.route) { FuelScreen() }
            composable(Screen.Charts.route) { ChartsScreen() }
            composable(Screen.Reminder.route) { ReminderScreen() }
            composable(Screen.Service.route) { ServiceScreen(navController) }
            composable(Routes.ADD_SERVICE) { AddServiceScreen(navController) }
            composable(Screen.Profile.route) { VehicleProfileScreen() }
            composable(
                route = Routes.SERVICE_DETAIL,
                arguments = listOf(
                    navArgument("serviceId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getLong("serviceId") ?: 0L
                ServiceDetailScreen(
                    serviceId = serviceId,
                    navController = navController
                )
            }
        }
    }
}