package com.henrythasler.sheetmusic

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
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
    data object Home : Screen("home")
    data object Browser : Screen("browser")
    data object Notation : Screen(route = "notation/{encodedFolderPath}/{filename}") {
        fun createRoute(folderPath: String, filename: String): String {
            // Encode the folder path to handle special characters like '/', '\', etc.
            val encodedFolder = Uri.encode(folderPath)
            val encodedFilename = Uri.encode(filename)
            return "notation/$encodedFolder/$encodedFilename"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar(
    navController: NavHostController,
    viewModel: VerovioViewModel,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "Verovio ${viewModel.verovioVersion.value}",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis

            )
        },
        navigationIcon = {
            IconButton(
                enabled = currentRoute != Screen.Browser.route,
                onClick = {
                    navController.navigate(Screen.Browser.route)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            DropdownMenuWithDetails()
//            IconButton(onClick = { /* do something */ }) {
//                Icon(
//                    imageVector = Icons.Filled.Menu,
//                    contentDescription = "Localized description"
//                )
//            }
        },
    )
}

@Composable
fun DropdownMenuWithDetails() {
    var expanded by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = !expanded }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("About") },
            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
            onClick = { openAlertDialog.value = true }
        )
    }

    when {
        // ...
        openAlertDialog.value -> {
            AlertDialog(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.verovio_logo_big),
                        contentDescription = "Example Icon"
                    )
                },
                title = {
                    Text(text = "Verovio Demo")
                },
                text = {
                    Text(text = "by Henry Thasler\n\nsee www.verovio.org")
                },
                onDismissRequest = {
                    openAlertDialog.value = false
                },
                confirmButton = {
                    TextButton (
                        onClick = {
                            openAlertDialog.value = false
                        }
                    ) {
                        Text("OK")
                    }
                },
            )
        }
    }
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