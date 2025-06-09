package com.henrythasler.sheetmusic

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.henrythasler.sheetmusic.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicViewerApp(
    widthSizeClass: WindowWidthSizeClass,
) {
    MyApplicationTheme(
//                darkTheme = true,
//                dynamicColor = false
    ) {
        val navController = rememberNavController()
        val viewModel = remember { VerovioViewModel() }
        val context = LocalContext.current

        LaunchedEffect(true) {
            viewModel.extractAssets(context)
            viewModel.getVerovioVersion()
        }

        Scaffold(
            topBar = {
                TopNavigationBar(
                    navController = navController,
                    viewModel = viewModel,
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Notation.createRoute("mei/tempo/tempo-003.mei", "tempo-003.mei"),
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(
                    route = Screen.Browser.route
                ) {
                    BrowserScreen(navController = navController, viewModel = viewModel)
                }
                composable(
                    route = Screen.Notation.route,
                    arguments = listOf(
                        navArgument("encodedFolderPath") { type = NavType.StringType },
                        navArgument("filename") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    // Extract and decode the arguments
                    val encodedFolderPath = backStackEntry.arguments?.getString("encodedFolderPath") ?: ""
                    val encodedFilename = backStackEntry.arguments?.getString("filename") ?: ""

                    // Decode the folder path
                    val assetPath = Uri.decode(encodedFolderPath)
                    val assetName = Uri.decode(encodedFilename)

                    NotationScreen(
                        navController = navController,
                        viewModel = viewModel,
                        assetPath = assetPath,
                        assetName = assetName
                    )
                }
                composable(
                    route = Screen.Settings.route
                ) {
                    SettingsScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}
