package com.henrythasler.sheetmusic

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.henrythasler.sheetmusic.ui.theme.MyApplicationTheme

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
        if(svgData.isNotEmpty()) {
            SvgImage(
                svgData,
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Green)
                )
        }
//        AsyncImage(
//            model = ImageRequest.Builder(context = LocalContext.current)
//                .data(ByteBuffer.wrap(svgData.toByteArray()))
//                .decoderFactory(SvgDecoder.Factory())
//                .build(),
//            placeholder = painterResource(R.drawable.baseline_image_search_24),
//            contentDescription = stringResource(R.string.description),
//            contentScale = ContentScale.FillWidth,
//            modifier = Modifier.fillMaxSize().border(1.dp, Color.Green)
//        )

    }
}

/**
 * A composable function that renders SVG content using AndroidSVG library.
 *
 * @param svgString The SVG content as a string
 * @param contentDescription Description for accessibility
 * @param modifier Modifier for the composable
 * @param contentScale How the SVG should be scaled within its bounds
 */
@Composable
fun SvgImage(
    svgString: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    // Parse SVG in a remember block to avoid reprocessing on recomposition
    val svg = remember(svgString) {
        try {
            SVG.getFromString(svgString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Only proceed if SVG was parsed successfully
    if (svg != null) {
        val density = LocalDensity.current.density

        // Get the document dimensions, defaulting to 100dp if not specified
        val svgWidth = if (svg.documentWidth != -1f) svg.documentWidth else 100f * density
        val svgHeight = if (svg.documentHeight != -1f) svg.documentHeight else 100f * density

        Canvas(
            modifier = modifier
                .clipToBounds() // Clip to ensure SVG stays within bounds
        ) {
            drawIntoCanvas { canvas ->
                // Calculate scaling based on ContentScale and available size
                val contentWidth = size.width
                val contentHeight = size.height

                // Calculate scaling factors for width and height
                val scaleX = contentWidth / svgWidth
                val scaleY = contentHeight / svgHeight

                // Calculate the scale factor based on ContentScale type
                val scale = when (contentScale) {
                    ContentScale.Fit -> minOf(scaleX, scaleY)
                    ContentScale.FillWidth -> scaleX
                    ContentScale.FillHeight -> scaleY
                    ContentScale.FillBounds -> 1f // Will use the viewBox transforms
                    ContentScale.Crop -> maxOf(scaleX, scaleY)
                    else -> minOf(scaleX, scaleY) // Default to Fit for other cases
                }

                // Calculate the centered position
                val scaledWidth = svgWidth * scale
                val scaledHeight = svgHeight * scale
                val left = (contentWidth - scaledWidth) / 2f
                val top = (contentHeight - scaledHeight) / 2f

                // Save the canvas state
                canvas.nativeCanvas.save()

                // Apply scaling and translation
                canvas.nativeCanvas.translate(left, top)
                canvas.nativeCanvas.scale(scale, scale)

                // Create rendering options
                val renderOptions = RenderOptions()

                // Apply tint if specified
//                tintColor?.let {
//                    renderOptions.css("svg { fill: #${it.toArgb().toUInt().toString(16).padStart(8, '0')} }")
//                }

                // Define the viewport for rendering
                val viewPort = RectF(0f, 0f, svgWidth, svgHeight)

                // Render the SVG to the canvas
                svg.renderToCanvas(canvas.nativeCanvas)

                // Restore the canvas state
                canvas.nativeCanvas.restore()
            }
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
