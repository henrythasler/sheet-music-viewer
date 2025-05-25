package com.henrythasler.sheetmusic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun NotationScreen(
    navController: NavHostController,
    viewModel: VerovioViewModel,
    folderPath: String,
    filename: String
) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.renderAsset(context, folderPath)
    }

    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

    if (svgData.isNotEmpty()) {
//        PannableCachedSvgImage(
//            assetName = filename,
//            svgString = svgData,
//            maxScale = 30f,
//            customFont = "Edwin-Roman",
////                tintColor = Color.hsv(80f, 1f, .5f),
//            modifier = Modifier
//                .fillMaxSize(),
//        )

        ScalableCachedSvgImage(
            assetName = filename,
            svgString = svgData,
            config = ResolutionConfig(0.2f, 12f, listOf(0.25f, 0.5f, 1f, 2f, 4f)),
            customFont = "Edwin-Roman",
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}