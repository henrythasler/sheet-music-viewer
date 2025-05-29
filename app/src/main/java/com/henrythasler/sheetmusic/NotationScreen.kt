package com.henrythasler.sheetmusic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

@Composable
fun NotationScreen(
    navController: NavHostController,
    viewModel: VerovioViewModel,
    assetPath: String,
    assetName: String
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.renderAsset(context, assetPath)
    }

    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

    if (svgData.isNotEmpty()) {
//        ZoomableSvgImage(
//            modifier = Modifier
//                .fillMaxSize(),
//            maxScale = 32f,
//            minScale = 0.25f,
//            svgString = svgData,
//            customFont = "Edwin-Roman",
//        )


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
            modifier = Modifier
                .fillMaxSize(),
            assetName = assetName,
            svgString = svgData,
            svgConfig = SvgConfig(null, "Edwin-Roman"),
            canvasConfig = CanvasConfig(0.25f, 32f, 200L),
        )
    }
}