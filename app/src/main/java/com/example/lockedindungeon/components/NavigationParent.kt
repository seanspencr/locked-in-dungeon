package com.example.lockedindungeon.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lockedindungeon.screens.BlockingListScreen
import com.example.lockedindungeon.screens.HomeScreen
import com.example.lockedindungeon.viewmodels.HomeViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.lockedindungeon.data.local.entities.TargetType
import com.example.lockedindungeon.screens.BlockingConfigurationScreen
import com.example.lockedindungeon.screens.SettingsScreen
import com.example.lockedindungeon.screens.UsageScreen
import com.example.lockedindungeon.viewmodels.BlockingConfigurationViewmodel
import com.example.lockedindungeon.viewmodels.BlockingListViewModel
import com.example.lockedindungeon.viewmodels.SettingsViewmodel
import com.example.lockedindungeon.viewmodels.UsageViewModel
import kotlinx.serialization.Serializable

@Composable
fun NavigationParent(controller: NavHostController , modifier: Modifier = Modifier) {
    val startRoute = NavigationRoute.HomeScreenRoute

    val homeViewModel : HomeViewModel = hiltViewModel()
    val usageViewModel : UsageViewModel = hiltViewModel()
    val blockingConfigurationViewmodel : BlockingConfigurationViewmodel = hiltViewModel()
    val blockingListViewmodel : BlockingListViewModel = hiltViewModel()
    val settingsViewmodel : SettingsViewmodel = hiltViewModel()
    NavHost(controller, startDestination = startRoute, modifier = modifier) {
        composable<NavigationRoute.HomeScreenRoute> {
            HomeScreen(
                viewModel = homeViewModel
            )
        }
        composable<NavigationRoute.BlockingListScreenRoute> {
            BlockingListScreen(viewModel = blockingListViewmodel, navigateToBlockingConfigurationScreen = {packageName, type -> controller.navigate(
                NavigationRoute.BlockingConfigurationRoute(packageName, type))})
        }

        composable<NavigationRoute.UsageScreenRoute> {
            UsageScreen(
                viewmodel = usageViewModel,
                navigateToBlockingConfigurationScreen = {
                    packageName, type -> controller.navigate(NavigationRoute.BlockingConfigurationRoute(packageName, type))
                }
            )
        }

        composable<NavigationRoute.BlockingConfigurationRoute> {
            backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.BlockingConfigurationRoute>()
            BlockingConfigurationScreen(
                packageName = route.packageName,
                onNavigateBack = {controller.navigateUp()},
                targetType = route.targetType,
                viewModel = blockingConfigurationViewmodel
            )
        }

        composable<NavigationRoute.SettingsScreenRoute> {
            backstackEntry ->
                SettingsScreen(viewmodel = settingsViewmodel)
        }
    }
}

@Composable
fun ParentNavigationBar(controller : NavHostController, modifier: Modifier = Modifier){

//    ngecek history navigasi buat tau yang terakhir dikunjungin yang class mana
    val navBackStackEntry by controller.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        NavigationBarRouteObject.entries.forEach {
            NavigationBarItem(
                selected = currentDestination?.hasRoute(it.route::class) ?: true,
                icon = { Icon(it.icon, it.description) },
                onClick = { controller.navigate(it.route) },
                label = { Text(it.description) }
            )
        }
    }
}

enum class NavigationBarRouteObject(
    val icon : ImageVector,
    val description : String,
    val route : NavigationRoute
) {
    HomeScreenRouteObject(Icons.Default.Home, "Home", NavigationRoute.HomeScreenRoute),
    BlockingListScreenRouteObject(Icons.Default.Menu, "Blocked", NavigationRoute.BlockingListScreenRoute),
    UsageScreenRouteObject(Icons.Default.BarChart, "Usage", NavigationRoute.UsageScreenRoute),
    SettingsScreenRouteObject(Icons.Default.Settings, "Settings", NavigationRoute.SettingsScreenRoute)
}

@Serializable
sealed interface NavigationRoute {
    @Serializable
    object HomeScreenRoute : NavigationRoute
    @Serializable
    object BlockingListScreenRoute : NavigationRoute
    @Serializable
    object UsageScreenRoute : NavigationRoute
    @Serializable
    data class BlockingConfigurationRoute(val packageName : String, val targetType : TargetType) : NavigationRoute
    @Serializable
    object SettingsScreenRoute : NavigationRoute
}