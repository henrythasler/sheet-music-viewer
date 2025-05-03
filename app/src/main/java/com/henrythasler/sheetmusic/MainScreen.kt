package com.henrythasler.sheetmusic

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.henrythasler.sheetmusic.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSkeleton() {
    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Menu") }
//            )
//        },
//        bottomBar = { },
//        floatingActionButton = { }
    ) { innerPadding ->
        MainScreen(
            modifier = Modifier.padding(innerPadding),
            viewModel = VerovioViewModel(),
        )
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier, viewModel: VerovioViewModel = VerovioViewModel()
) {
    val context = LocalContext.current
    val verovioVersion = viewModel.verovioVersion.value
    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.getVerovioVersion();
        viewModel.loadAsset(context, "data")
        viewModel.renderData()
    }

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
            )
        }
//        }
    }
}
