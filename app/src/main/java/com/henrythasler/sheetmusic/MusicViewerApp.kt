package com.henrythasler.sheetmusic

import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
        // Create repository and viewmodel directly
        val settingsRepository = remember { SettingsRepository(context) }
        val settingsViewModel = remember { SettingsViewModel(settingsRepository) }

        LaunchedEffect(Unit) {
            viewModel.extractAssets(context)
            viewModel.getVerovioVersion()
        }

        CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
            ) {
                composable(route = Screen.Browser.route) {
                    BrowserScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToNotation = { filename ->
                            navController.navigate(Screen.Notation.createRoute(filename, ""))
                        },
                        uiState = viewModel.uiState.collectAsState().value,
                        meiAssetsFolder = viewModel.meiAssetsFolder.collectAsState().value,
                        readAssets = viewModel::readAssets,
                    )
                }

                composable(
                    route = Screen.Notation.route,
                    arguments = listOf(
                        navArgument("encodedFolderPath") { type = NavType.StringType },
                        navArgument("filename") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    // Extract and decode the arguments
                    val encodedFolderPath =
                        backStackEntry.arguments?.getString("encodedFolderPath") ?: ""
                    val encodedFilename = backStackEntry.arguments?.getString("filename") ?: ""

                    // Decode the folder path
                    val assetPath = Uri.decode(encodedFolderPath)
                    val assetName = Uri.decode(encodedFilename)

                    NotationScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        engraveMusicAsset = viewModel::engraveMusicAsset,
                        assetPath = assetPath,
                    )
                }

                composable(route = Screen.Settings.route) {
                    SettingsScreen()
                }

                composable(route = Screen.Home.route) {
                    HomeScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBrowser = { navController.navigate(Screen.Browser.route) },
                        onNavigateToNotation = { filename ->
                            navController.navigate(Screen.Notation.createRoute(filename, ""))
                                               },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        verovioVersion = viewModel.verovioVersion.value,
                    )
                }
            }
        }
    }
}
