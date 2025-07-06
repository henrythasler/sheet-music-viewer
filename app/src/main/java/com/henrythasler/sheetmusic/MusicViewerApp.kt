package com.henrythasler.sheetmusic

import android.net.Uri
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.henrythasler.sheetmusic.ui.theme.MyApplicationTheme
import kotlin.system.measureTimeMillis

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
        val settingsViewModel: SettingsViewModel = viewModel(
            factory = SettingsViewModelFactory(context)
        )

        LaunchedEffect(Unit) {
            val renderTime = measureTimeMillis {
                viewModel.extractAssets(context)
            }
            Log.d("STARTUP", "extractAssets in $renderTime ms")

            viewModel.getVerovioVersion()
            SvgCustomFonts.forEach { (_, customFont) ->
                if(customFont.path != null) {
                    customFont.fontFamily = FontFamily(
                        Font(
                            path = customFont.path,
                            assetManager = context.assets,
                            weight = FontWeight.Normal,
                            style = FontStyle.Normal,
                        )
                    )
                }
            }
        }

        CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
            val settings = useSettings()
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
            ) {
                composable(route = Screen.Browser.route) {
                    BrowserScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToNotation = { filename ->
                            settings.currentOffset = Offset.Zero
                            settings.currentScale = 1.0f
                            navController.navigate(Screen.Notation.createRoute(filename, 1.0f, Offset.Zero))
                        },
                        uiState = viewModel.uiState.collectAsState().value,
                        meiAssetsFolder = viewModel.meiAssetsFolder.collectAsState().value,
                        readAssets = viewModel::readAssets,
                        verovioVersion = viewModel.verovioVersion.value,
                    )
                }

                composable(
                    route = Screen.Notation.route,
                    arguments = listOf(
                        navArgument("encodedAssetPath") { type = NavType.StringType },
                        navArgument("scale") { type = NavType.FloatType },
                        navArgument("x") { type = NavType.FloatType },
                        navArgument("y") { type = NavType.FloatType },
                        navArgument("encodedTag") { type = NavType.StringType },
                    )
                ) { backStackEntry ->
                    // Extract and decode the arguments
                    val encodedAssetPath = backStackEntry.arguments?.getString("encodedAssetPath") ?: ""
                    val scale = backStackEntry.arguments?.getFloat("scale", 1.0f) ?: 1.0f
                    val offsetX = backStackEntry.arguments?.getFloat("x", 0.0f) ?: 0.0f
                    val offsetY = backStackEntry.arguments?.getFloat("y", 0.0f) ?: 0.0f
                    val encodedTag = backStackEntry.arguments?.getString("encodedTag") ?: ""

                    // Decode the folder path
                    val assetPath = Uri.decode(encodedAssetPath)
                    val tag = Uri.decode(encodedTag)

                    NotationScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        engraveMusicAsset = viewModel::engraveMusicAsset,
                        assetPath = assetPath,
                        initialScale = scale,
                        initialOffset = Offset(offsetX, offsetY),
                        verovioVersion = viewModel.verovioVersion.value,
                    )
                }

                composable(route = Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }

                composable(route = Screen.Home.route) {
                    HomeScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBrowser = { navController.navigate(Screen.Browser.route) },
                        onNavigateToNotation = { filename ->
                            settings.currentOffset = Offset.Zero
                            settings.currentScale = 1.0f
                            navController.navigate(Screen.Notation.createRoute(filename, 1.0f, Offset.Zero))
                                               },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        verovioVersion = viewModel.verovioVersion.value,
                    )
                }
            }
        }
    }
}
