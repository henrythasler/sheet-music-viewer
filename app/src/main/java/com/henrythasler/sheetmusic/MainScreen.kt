package com.henrythasler.sheetmusic

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
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
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Top app bar")
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom app bar",
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
//                    modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        MainScreen(
//                        name = stringFromJNI(),
            modifier = Modifier.padding(innerPadding),
            viewModel = VerovioViewModel(),
//                        svgSource = RenderData(),
        )
    }
}


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
        viewModel.renderData()
    }

    Column(
//        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Verovio $verovioVersion",
            modifier = modifier
        )
        if(svgData.isNotEmpty()) {
            SvgImage(
                svgData,
                modifier = modifier
//                    .width(400.dp)
//                    .height((400.dp))
                    .fillMaxSize()
                    .border(1.dp, Color.Red)
                )
        }
//        SvgImage(
//            "<svg version=\"1.2\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 457 120\" width=\"457\" height=\"120\">\n" +
//                    "\t<title>Sample.Cat</title>\n" +
//                    "\t<style>\n" +
//                    "\t\t.s0 { fill: #293036 } \n" +
//                    "\t\t.s1 { fill: #218ae3 } \n" +
//                    "\t</style>\n" +
//                    "\t<path id=\"Sample.Cat\" class=\"s0\" aria-label=\"Sample.Cat\"  d=\"m138.7 68.8q0-2.2-1.7-3.6-1.6-1.4-4.1-2.5-2.4-1.2-5.4-2.4-2.8-1.3-5.4-3-2.4-1.8-4.1-4.5-1.6-2.6-1.6-6.8 0-3.6 1.2-6.1 1.2-2.7 3.3-4.5 2.3-1.7 5.3-2.5 3-0.9 6.7-0.9 4.3 0 8.1 0.8 3.7 0.7 6.2 2.1l-3 8q-1.6-0.9-4.6-1.7-3.1-0.9-6.7-0.9-3.3 0-5.2 1.4-1.7 1.3-1.7 3.6 0 2 1.6 3.5 1.7 1.4 4.1 2.5 2.5 1.2 5.4 2.5 3 1.2 5.4 3.1 2.5 1.7 4.1 4.4 1.7 2.6 1.7 6.5 0 3.9-1.3 6.8-1.3 2.8-3.7 4.7-2.3 1.9-5.6 2.8-3.3 0.9-7.3 0.9-5.3 0-9.2-1-4-1-5.9-2l3.1-8.2q0.7 0.5 1.9 1 1.3 0.5 2.8 0.9 1.6 0.4 3.2 0.7 1.8 0.3 3.6 0.3 4.3 0 6.5-1.4 2.3-1.5 2.3-4.5zm17.7-13l-2.1-6.8q2.8-1.2 6.6-1.9 3.9-0.8 8.1-0.8 3.6 0 6.1 0.9 2.4 0.8 3.8 2.5 1.5 1.6 2 3.8 0.7 2.3 0.7 5 0 3.1-0.2 6.3-0.3 3.1-0.3 6.1 0 3 0.2 5.9 0.2 2.7 1 5.3h-7.4l-1.5-4.9h-0.3q-1.4 2.2-3.9 3.8-2.5 1.6-6.4 1.6-2.4 0-4.4-0.7-2-0.8-3.4-2.1-1.4-1.4-2.1-3.3-0.8-1.8-0.8-4.2 0-3.2 1.4-5.3 1.5-2.3 4.1-3.6 2.8-1.4 6.5-1.9 3.7-0.6 8.4-0.4 0.5-3.9-0.6-5.6-1-1.7-4.7-1.7-2.7 0-5.8 0.5-3 0.6-5 1.5zm9.4 19.3q2.7 0 4.4-1.2 1.6-1.3 2.3-2.7v-4.5q-2.1-0.2-4.2-0.1-1.9 0.1-3.5 0.6-1.5 0.5-2.4 1.4-0.9 0.9-0.9 2.3 0 2 1.1 3.1 1.2 1.1 3.2 1.1zm52.4 6.9h-9.1v-19q0-4.9-0.9-6.9-0.9-2-3.8-2-2.4 0-3.9 1.3-1.4 1.3-2.2 3.3v23.3h-9.1v-35h7.1l1 4.6h0.3q1.6-2.2 4.1-3.9 2.5-1.7 6.4-1.7 3.4 0 5.5 1.4 2.1 1.4 3.3 4.6 1.6-2.8 4.1-4.4 2.6-1.6 6.1-1.6 3 0 5 0.7 2.1 0.7 3.4 2.5 1.3 1.6 1.9 4.5 0.7 2.9 0.7 7.3v21h-9.1v-19.7q0-4.1-1-6.1-0.8-2.1-3.8-2.1-2.5 0-3.9 1.3-1.4 1.3-2.1 3.6zm27.3 14v-49h6.7l1 4.2h0.3q1.8-2.6 4.3-3.9 2.5-1.2 6.1-1.2 6.6 0 9.9 4.2 3.2 4.1 3.2 13.3 0 4.5-1 8.1-1.1 3.6-3.2 6.1-2 2.6-5 3.9-2.9 1.3-6.8 1.3-2.2 0-3.6-0.3-1.4-0.3-2.8-1v14.3zm15.7-42.2q-2.7 0-4.2 1.3-1.5 1.3-2.4 4v14.5q1 0.8 2.1 1.3 1.2 0.4 3.1 0.4 3.9 0 5.9-2.7 2-2.8 2-9.2 0-4.6-1.6-7.1-1.5-2.5-4.9-2.5zm31.5-20.8v37.3q0 2.4 0.7 3.6 0.6 1.1 2 1.1 0.8 0 1.6-0.2 0.8-0.1 2-0.6l1 7.1q-1.1 0.6-3.4 1.2-2.3 0.5-4.8 0.5-4 0-6.1-1.8-2.1-1.9-2.1-6.2v-42zm36.2 40.1l3 5.9q-2.1 1.7-5.7 2.9-3.6 1.1-7.6 1.1-8.5 0-12.4-4.9-4-4.9-4-13.6 0-9.2 4.5-13.8 4.4-4.6 12.3-4.6 2.7 0 5.2 0.7 2.5 0.7 4.5 2.3 2 1.6 3.2 4.3 1.1 2.7 1.1 6.8 0 1.5-0.2 3.2-0.1 1.6-0.5 3.5h-21q0.3 4.4 2.3 6.6 2.1 2.2 6.7 2.2 2.9 0 5.1-0.8 2.3-0.9 3.5-1.8zm-10-19.8q-3.6 0-5.3 2.2-1.7 2.1-2 5.7h13q0.3-3.8-1.2-5.8-1.4-2.1-4.5-2.1zm19.5 24.1q0-2.4 1.6-3.8 1.5-1.5 4-1.5 2.7 0 4.2 1.5 1.6 1.4 1.6 3.8 0 2.5-1.6 3.9-1.5 1.5-4.2 1.5-2.5 0-4-1.5-1.6-1.4-1.6-3.9zm49.7-5.2l2 7.8q-2.2 1.6-5.7 2.3-3.5 0.7-7.1 0.7-4.4 0-8.4-1.3-3.9-1.4-6.9-4.4-3.1-3.1-4.9-7.9-1.7-4.9-1.7-11.9 0-7.2 1.9-12 2.1-4.9 5.2-7.8 3.2-3.1 7.2-4.3 3.9-1.3 7.8-1.3 4.2 0 7.1 0.5 2.9 0.6 4.8 1.4l-1.9 8.1q-1.6-0.8-3.8-1.1-2.2-0.4-5.4-0.4-5.8 0-9.3 4.1-3.5 4.2-3.5 12.8 0 3.8 0.8 7 0.8 3 2.5 5.3 1.8 2.2 4.3 3.4 2.6 1.2 5.9 1.2 3.2 0 5.4-0.6 2.1-0.6 3.7-1.6zm8.9-16.3l-2.1-6.8q2.8-1.3 6.6-2 3.9-0.8 8.1-0.8 3.6 0 6 0.9 2.5 0.9 3.9 2.5 1.5 1.6 2 3.9 0.7 2.2 0.7 5 0 3.1-0.3 6.2-0.2 3.1-0.2 6.1 0 3 0.2 5.9 0.2 2.8 1 5.3h-7.4l-1.5-4.8h-0.3q-1.4 2.2-3.9 3.8-2.5 1.5-6.4 1.5-2.5 0-4.4-0.7-2-0.8-3.4-2.1-1.4-1.4-2.1-3.2-0.8-1.9-0.8-4.2 0-3.2 1.4-5.4 1.5-2.3 4.1-3.6 2.7-1.4 6.5-1.9 3.7-0.5 8.4-0.3 0.4-3.9-0.6-5.6-1.1-1.8-4.7-1.8-2.7 0-5.8 0.6-3 0.6-5 1.5zm9.4 19.2q2.7 0 4.3-1.2 1.7-1.2 2.4-2.6v-4.6q-2.1-0.2-4.2-0.1-1.9 0.2-3.5 0.7-1.5 0.5-2.4 1.4-0.9 0.9-0.9 2.3 0 1.9 1.1 3.1 1.2 1 3.2 1zm19.7-20.4v-7.7h4.9v-6.5l9.1-2.6v9.1h8.5v7.7h-8.5v13.5q0 3.6 0.7 5.2 0.7 1.6 2.8 1.6 1.4 0 2.4-0.3 1-0.3 2.3-0.8l1.6 7q-1.9 0.9-4.4 1.5-2.6 0.6-5.1 0.6-4.9 0-7.2-2.4-2.2-2.5-2.2-8.2v-17.7z\"/>\n" +
//                    "\t<g>\n" +
//                    "\t\t<path fill-rule=\"evenodd\" class=\"s1\" d=\"m92 64.3c0 20.3 0 30.5-6.3 36.9-6.4 6.3-16.6 6.3-37 6.3-20.3 0-30.5 0-36.9-6.3-6.3-6.4-6.3-16.6-6.3-36.9 0-20.4 0-30.6 6.3-36.9-1.1-6.8-1.4-13.5 1.4-14.6 5.5-2.4 18.6 0.6 26.3 8.2q4.2 0 9.2 0 5.5 0 10.1 0c7.7-7.5 20.3-10.5 25.9-8.2 2.8 1.2 2.5 8.1 1.4 15 5.9 6.4 5.9 16.6 5.9 36.5zm-55-18.9l-0.4 0.4-9.7 9.5-0.3 0.2c-1.8 1.8-3 3-3.9 4.2q-1.5 2.1-2.3 4.6-0.8 2.5-0.8 5.1 0 2.6 0.8 5.1 0.8 2.5 2.3 4.6c0.9 1.3 2.1 2.5 3.9 4.3l0.3 0.2c0.6 0.6 1.4 0.9 2.3 0.9 0.9 0 1.7-0.3 2.3-0.9 0.6-0.7 0.9-1.5 0.9-2.4 0-0.8-0.4-1.7-1-2.3-2.2-2.1-2.9-2.8-3.4-3.5q-1-1.3-1.5-2.9-0.5-1.5-0.5-3.1 0-1.6 0.5-3.1 0.5-1.5 1.5-2.8c0.5-0.7 1.2-1.5 3.4-3.6l9.7-9.4c3.9-3.8 5.3-5.1 6.7-5.8q1.1-0.6 2.4-0.9 1.2-0.3 2.5-0.3 1.3 0 2.6 0.3 1.2 0.3 2.4 0.9c1.4 0.7 2.8 2 6.7 5.8 3.8 3.8 5.2 5.1 5.9 6.5q0.6 1.1 0.9 2.3 0.3 1.2 0.3 2.4 0 1.2-0.3 2.4-0.3 1.2-0.9 2.3c-0.7 1.4-2.1 2.7-5.9 6.5l-9.6 9.3c-1 0.9-1.2 1.2-1.5 1.4q-0.7 0.5-1.5 0.8-0.8 0.2-1.7 0.2-0.8 0-1.6-0.2-0.9-0.3-1.5-0.8c-0.3-0.2-0.6-0.4-1.5-1.4-1-1-1.3-1.3-1.5-1.5q-0.5-0.6-0.8-1.4-0.2-0.7-0.2-1.6 0-0.8 0.2-1.5 0.3-0.8 0.8-1.4c0.2-0.3 0.5-0.5 1.5-1.5l7.3-7.1c0.6-0.7 0.9-1.5 1-2.3 0-0.9-0.4-1.7-1-2.3-0.6-0.6-1.4-1-2.2-1-0.9 0-1.7 0.3-2.3 0.9l-7.4 7.2-0.1 0.1c-0.8 0.8-1.4 1.3-1.9 1.9q-1.2 1.5-1.8 3.3-0.6 1.8-0.6 3.7 0 1.9 0.6 3.7 0.6 1.8 1.8 3.3c0.5 0.6 1.1 1.2 1.9 2l0.1 0.1 0.2 0.2q0.2 0.2 0.5 0.4 0.2 0.3 0.4 0.5 0.3 0.2 0.5 0.5 0.3 0.2 0.6 0.4 1.5 1.1 3.3 1.7 1.8 0.6 3.7 0.6 1.9 0 3.8-0.6 1.8-0.6 3.3-1.7c0.6-0.5 1.2-1.1 2-1.8l0.2-0.2 9.5-9.2 0.4-0.5c3.3-3.2 5.5-5.3 6.7-7.7q1-1.8 1.5-3.7 0.4-2 0.4-4 0-2-0.4-4-0.5-1.9-1.5-3.7c-1.2-2.4-3.4-4.5-6.7-7.7l-0.4-0.5-0.5-0.4c-3.2-3.2-5.4-5.3-7.9-6.5q-1.8-0.9-3.8-1.4-1.9-0.4-4-0.4-2 0-4 0.4-2 0.5-3.8 1.4c-2.4 1.2-4.6 3.3-7.9 6.5z\"/>\n" +
//                    "\t</g>\n" +
//                    "</svg>",
//            modifier = Modifier
////                .fillMaxSize()
//                .width(400.dp)
//                .height((200.dp))
//                .border(1.dp, Color.Green)
//        )
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
//                val renderOptions = RenderOptions()

                // Apply tint if specified
//                tintColor?.let {
//                    renderOptions.css("svg { fill: #${it.toArgb().toUInt().toString(16).padStart(8, '0')} }")
//                }

                // Define the viewport for rendering
//                val viewPort = RectF(0f, 0f, svgWidth, svgHeight)

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
