package com.henrythasler.sheetmusic

import android.net.Uri
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

sealed class Screen(val route: String) {
    data object Browser : Screen("browser")
    data object Notation : Screen(route = "notation/{encodedAssetPath}/{scale}/{y}/{x}/{encodedTag}") {
        fun createRoute(assetPath: String, scale: Float, offset: Offset, tag: String = ""): String {
            // Encode the folder path to handle special characters like '/', '\', etc.
            val encodedAssetPath = Uri.encode(assetPath)
            val encodedTag = Uri.encode(tag)
            return "notation/$encodedAssetPath/$scale/${offset.y}/${offset.x}/$encodedTag"
        }
    }
    data object Settings : Screen("settings")
    data object Home : Screen("home")
}


@Composable
fun BottomNavigationBar(navController: NavHostController, items: List<NavigationItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when navigating back
                        restoreState = true
                    }
                }
            )
        }
    }
}