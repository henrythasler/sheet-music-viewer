package com.henrythasler.sheetmusic

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

//    Scaffold { innerPadding ->
    val verovioVersion = viewModel.verovioVersion.value
    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

    Column {
        Row {
            Button(
                onClick = {
                    navController.navigate(Screen.Browser.route)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "back"
                )
            }
            Column {
                Text(
                    text = "Verovio $verovioVersion",
                )

                Text(
                    text = "Item: $folderPath"
                )
            }
        }

        if (svgData.isNotEmpty()) {
            PannableCachedSvgImage(
                assetName = filename,
                svgString = svgData,
                customFont = "Edwin-Roman",
//                tintColor = Color.hsv(80f, 1f, .5f),
                modifier = Modifier
                    .fillMaxSize(),
            )
//                ZoomableSvgImage(
//                    modifier = Modifier
//                        .fillMaxSize(),
////                        .border(5.dp, MaterialTheme.colorScheme.primaryContainer),
//                    svgString = svgData,
//                    panLimitFactor = 0.5f,
////                    tintColor = MaterialTheme.colorScheme.error,
//                    customFont = "Edwin-Roman",
//                )
        }
    }
//    }
}