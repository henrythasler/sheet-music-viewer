package com.henrythasler.sheetmusic

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object MusicViewerDestinations {
    const val FILES = "files"
    const val SHEET = "sheet"
}

//sealed class Screen(val route: String) {
//    object Home : Screen("home")
//    object Browser : Screen("browser")
//    object Settings : Screen("settings")
//    object Notation : Screen(route = "notation/{itemId}") {
//        fun createRoute(itemId: String) = "notation/$itemId"
//    }
//}

/**
 * Models the navigation actions in the app.
 */
class MusicViewerNavigationActions(navController: NavHostController) {
    val navigateToFiles: () -> Unit = {
        navController.navigate(MusicViewerDestinations.FILES) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
    val navigateToSheetMusic: () -> Unit = {
        navController.navigate(MusicViewerDestinations.SHEET) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}