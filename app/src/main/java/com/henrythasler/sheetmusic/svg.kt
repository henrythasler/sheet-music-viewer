package com.henrythasler.sheetmusic

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import androidx.core.graphics.withTranslation

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
    modifier: Modifier = Modifier,
    svgString: String,
    tintColor: Color? = null,
    contentDescription: String? = null,
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
            modifier = modifier.clipToBounds() // Clip to ensure SVG stays within bounds
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
                tintColor?.let {
                    renderOptions.css("svg { fill: #${it.toArgb().toUInt().toString(16).padStart(8, '0')} }")
                }

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


/**
 * A composable that renders an SVG with pinch-zoom and pan functionality
 */
@Composable
fun ZoomableSvgImage(
    modifier: Modifier = Modifier,
    svgString: String,
    minScale: Float = 0.5f,
    maxScale: Float = 5f
) {
    val svg = remember(svgString) {
        try {
            SVG.getFromString(svgString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    if (svg != null) {
        // State for zoom and pan
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        // Get SVG dimensions or use defaults
        val svgWidth = if (svg.documentWidth != -1f) svg.documentWidth else 100f
        val svgHeight = if (svg.documentHeight != -1f) svg.documentHeight else 100f

        Box(
            modifier = modifier
                .clipToBounds()
                .border(1.dp, Color.Red)
                // Handle pinch-zoom and pan gestures
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Update scale with constraints
                        scale = (scale * zoom)//.coerceIn(minScale, maxScale)
                        // Update offset
                        offset += pan
                        offset *= zoom

//                        Log.i("SVG", "pan: $pan, zoom: $zoom, scale: $scale, offset: $offset")
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Apply current scale and offset
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            ) {
                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas

                    // Calculate initial centering
                    val viewportWidth = size.width
                    val viewportHeight = size.height

                    // Center the SVG in the available space (before any user zooming/panning)
                    val initialScaleX = viewportWidth / svgWidth
                    val initialScaleY = viewportHeight / svgHeight
                    val initialScale = minOf(initialScaleX, initialScaleY)

                    val centeringX = (viewportWidth - svgWidth * initialScale) / 2f
                    val centeringY = (viewportHeight - svgHeight * initialScale ) / 2f

                    // Save canvas state
                    nativeCanvas.withTranslation(centeringX, centeringY) {

                        // Apply initial centering transform
                        scale(initialScale, initialScale)

                        // Set up render options
                        val renderOptions = RenderOptions()

                        // Render the SVG
                        svg.renderToCanvas(this, renderOptions)

                        // Restore canvas state
                    }
                }
            }
        }
    }
}