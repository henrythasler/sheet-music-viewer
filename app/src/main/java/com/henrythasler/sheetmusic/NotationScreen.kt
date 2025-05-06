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
import androidx.navigation.NavHostController

@Composable
fun NotationScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: VerovioViewModel = VerovioViewModel(),
    itemId: String?
) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.getVerovioVersion();
        viewModel.loadAsset(context, "data")
        viewModel.renderData()
    }

    Scaffold { innerPadding ->
        val verovioVersion = viewModel.verovioVersion.value
        val svgData by viewModel.svgData.collectAsStateWithLifecycle()

        Column(
            modifier = modifier.padding(innerPadding)
        ) {
            Text(
                text = "Verovio $verovioVersion", modifier = modifier
            )

            Text(
                text = "$itemId"
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
        }
    }
}