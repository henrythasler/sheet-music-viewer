package com.henrythasler.sheetmusic

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.withTranslation
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG

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
                    val color = String.format("#%02X%02X%02X%02X",
                        (it.red * 255).toInt(),
                        (it.green * 255).toInt(),
                        (it.blue * 255).toInt(),
                        (it.alpha * 255).toInt())
                    Log.i("SVG", color)
                    renderOptions
                        .css(
                            "svg { fill: $color;} path { color: $color;}")
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
    tintColor: Color? = null,
    minScale: Float = 0.5f,
    maxScale: Float = 10f
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
        // State for zoom and pan
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        // Get SVG dimensions or use defaults
        val svgWidth = if (svg.documentWidth != -1f) svg.documentWidth else 100f
        val svgHeight = if (svg.documentHeight != -1f) svg.documentHeight else 100f

        // Calculate the initial scale factor (to fit SVG in the view)
        var canvasSize by remember { mutableStateOf(Offset.Zero) }

        Box(
            modifier = modifier
                .clipToBounds()
                // Handle pinch-zoom and pan gestures
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        // Calculate new scale with constraints
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                        // Calculate the center of the canvas
                        val canvasCenterX = canvasSize.x / 2f
                        val canvasCenterY = canvasSize.y / 2f

                        // Calculate the position relative to the center of the canvas
                        val relativeX = centroid.x - canvasCenterX
                        val relativeY = centroid.y - canvasCenterY

                        // Calculate the focus point considering both the current offset and the centered position
                        val focusX = relativeX - offset.x
                        val focusY = relativeY - offset.y

                        // Calculate new offset to keep the pinch centroid at the same location after scaling
                        val newOffset = Offset(
                            offset.x + focusX - focusX * (newScale / scale),
                            offset.y + focusY - focusY * (newScale / scale)
                        )

                        // Update scale and offset
                        scale = newScale
                        offset = newOffset + pan
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
                    .onSizeChanged {
                        canvasSize = Offset(it.width.toFloat(), it.height.toFloat())
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
                    val centeringY = (viewportHeight - svgHeight * initialScale) / 2f

                    // Save canvas state
                    nativeCanvas.withTranslation(centeringX, centeringY) {

                        // Apply initial centering transform
                        scale(initialScale, initialScale)

                        // Set up render options
                        val renderOptions = RenderOptions()

                        // Apply tint if specified
                        tintColor?.let {
                            val color = String.format("#%02X%02X%02X%02X",
                                (it.red * 255).toInt(),
                                (it.green * 255).toInt(),
                                (it.blue * 255).toInt(),
                                (it.alpha * 255).toInt())
                            Log.i("SVG", color)
                            renderOptions
                                .css(
                                    "svg { fill: $color;} path { color: $color;}")
                        }

                        // Render the SVG
                        svg.renderToCanvas(this, renderOptions)
                    }
                }
            }
        }
    }
}