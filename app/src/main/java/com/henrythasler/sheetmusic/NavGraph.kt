package com.henrythasler.sheetmusic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

//const val POST_ID = "postId"

@Composable
fun MusicViewerNavGraph(
//    appContainer: AppContainer,
    isExpandedScreen: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    openDrawer: () -> Unit = {},
    startDestination: String = MusicViewerDestinations.FILES,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = MusicViewerDestinations.FILES,
//            deepLinks = listOf(
//                navDeepLink {
//                    uriPattern =
//                        "$JETNEWS_APP_URI/${JetnewsDestinations.HOME_ROUTE}?$POST_ID={$POST_ID}"
//                }
//            )
        ) { navBackStackEntry ->
//            val homeViewModel: HomeViewModel = viewModel(
//                factory = HomeViewModel.provideFactory(
//                    postsRepository = appContainer.postsRepository,
//                    preSelectedPostId = navBackStackEntry.arguments?.getString(POST_ID)
//                )
//            )
//            HomeRoute(
//                homeViewModel = homeViewModel,
//                isExpandedScreen = isExpandedScreen,
//                openDrawer = openDrawer,
//            )
            FileBrowser()
        }
        composable(MusicViewerDestinations.SHEET) {
//            val interestsViewModel: InterestsViewModel = viewModel(
//                factory = InterestsViewModel.provideFactory(appContainer.interestsRepository)
//            )
//            InterestsRoute(
//                interestsViewModel = interestsViewModel,
//                isExpandedScreen = isExpandedScreen,
//                openDrawer = openDrawer
//            )
            SheetViewer()
        }
    }
}