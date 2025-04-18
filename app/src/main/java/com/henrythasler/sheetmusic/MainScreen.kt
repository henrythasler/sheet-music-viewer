package com.henrythasler.sheetmusic

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.henrythasler.sheetmusic.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import java.io.InputStream
import java.nio.ByteBuffer

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: VerovioViewModel = VerovioViewModel()
) {
    val context = LocalContext.current
    val verovioVersion = viewModel.verovioVersion.value
    val svgData by viewModel.svgData.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.getVerovioVersion();
        viewModel.loadAsset(context, "data")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Verovio $verovioVersion",
            modifier = modifier
        )
//        Text(
//            text = svgData,
//            modifier = modifier
//        )
        SmallFloatingActionButton(
            onClick = {
                viewModel.renderData()
            }
        ) {
            Icon(Icons.Filled.Search, "")
        }
        Image(
            painter = painterResource(id = R.drawable.baseline_image_search_24),
            contentDescription = null
        )
        if(svgData.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(ByteBuffer.wrap(svgData.toByteArray()))
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                placeholder = painterResource(R.drawable.baseline_image_search_24),
                contentDescription = stringResource(R.string.description),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}
