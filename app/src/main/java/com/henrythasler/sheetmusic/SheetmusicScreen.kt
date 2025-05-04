package com.henrythasler.sheetmusic

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SheetViewer(
    modifier: Modifier = Modifier, viewModel: VerovioViewModel = VerovioViewModel()
) {
//    Text(text = "Verovio", modifier = modifier)

    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.getVerovioVersion();
        viewModel.loadAsset(context, "data")
        viewModel.renderData()
    }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Menu") }
//            )
//        },
//        bottomBar = { },
//        floatingActionButton = { }
    ) { innerPadding ->
        SheetMusicFrame(
            modifier = modifier.padding(innerPadding),
            viewModel = viewModel
        )
    }
}


@Composable
fun SheetMusicFrame(
    modifier: Modifier = Modifier, viewModel: VerovioViewModel = VerovioViewModel()
) {
    val verovioVersion = viewModel.verovioVersion.value
    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

//        Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Menu") }
//            )
//        },
//        bottomBar = { },
//        floatingActionButton = { }
//        ) { innerPadding ->
//    BoxWithConstraints(
//        modifier = modifier
//    ) {
//        val boxMinWidth = minWidth;
//        val boxMaxWidth = maxWidth;
//        val boxMinHeight = minHeight;
//        val boxMaxHeight = maxHeight;
            Column(
                modifier = modifier
            ) {
//            Text("width: $boxMinWidth..$boxMaxWidth\nheight: $boxMinHeight..$boxMaxHeight")
                Text(
                    text = "Verovio $verovioVersion", modifier = modifier
                )


                if (svgData.isNotEmpty()) {
                    ZoomableSvgImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(5.dp, MaterialTheme.colorScheme.primaryContainer),
                        svgString = svgData,
//                tintColor = MaterialTheme.colorScheme.error
                    )
                }
//        }
            }
//        }




}