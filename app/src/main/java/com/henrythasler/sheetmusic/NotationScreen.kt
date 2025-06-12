package com.henrythasler.sheetmusic

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlin.system.measureTimeMillis

@Composable
fun NotationScreen(
    navController: NavHostController,
    viewModel: VerovioViewModel,
    assetPath: String,
    assetName: String
) {
    val context = LocalContext.current
    var svgDocument by remember { mutableStateOf<String?>(null) }
    var engraveTimeMillis by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        engraveTimeMillis = measureTimeMillis {
            svgDocument = viewModel.engraveMusicAsset(context, assetPath)
        }
        Log.i("Verovio", "Engraving '$assetPath' took $engraveTimeMillis ms. (${svgDocument?.length} Bytes)")
    }

    svgDocument?.let { svg ->
        ScalableCachedSvgImage(
            modifier = Modifier
                .fillMaxSize(),
            title = "$assetName ($engraveTimeMillis ms, ${svg.length/1024} KiB)",
            svgDocument = svg,
            svgConfig = SvgConfig(null, "Edwin-Roman"),
            canvasConfig = CanvasConfig(0.25f, 32f, 100L),
        )
    }
}