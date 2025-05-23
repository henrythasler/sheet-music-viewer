package com.henrythasler.sheetmusic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.withTranslation
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGExternalFileResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.delay

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
                    val color = String.format(
                        "#%02X%02X%02X%02X",
                        (it.red * 255).toInt(),
                        (it.green * 255).toInt(),
                        (it.blue * 255).toInt(),
                        (it.alpha * 255).toInt()
                    )
                    Log.i("SVG", color)
                    renderOptions
                        .css(
                            "svg { fill: $color;} path { color: $color;}"
                        )
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
 * Custom resolver for AndroidSVG to resolve fonts that must be loaded from assets.
 * Caches loaded fonts to improve performance
 */
class FastFontResolver(context: Context, assetFolder: String = "") : SVGExternalFileResolver() {
    // Member Variables
    private val _context: Context = context
    private val _folder: String = assetFolder
    private val fontCache = mutableMapOf<String, Typeface>()

    override fun resolveFont(fontFamily: String, fontWeight: Int, fontStyle: String): Typeface? {
        if (this.fontCache.containsKey(fontFamily)) {
            Log.d("FastFontResolver", "cache hit for $fontFamily ($fontWeight, $fontStyle)")
            return this.fontCache[fontFamily]
        }

        val fullPath = Paths.get(this._folder, fontFamily)
        Log.d("FastFontResolver", "caching $fontFamily ($fontWeight, $fontStyle) from $fullPath")
        try {
            this.fontCache[fontFamily] = Typeface.createFromAsset(_context.assets, "$fullPath.ttf")
            return this.fontCache[fontFamily]
        } catch (_: Exception) {
        }

        try {
            this.fontCache[fontFamily] = Typeface.createFromAsset(_context.assets, "$fullPath.otf")
            return this.fontCache[fontFamily]
        } catch (_: Exception) {
        }

        Log.e("FastFontResolver", "Could not resolve $fontFamily ($fontWeight, $fontStyle)")

        return null
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
    maxScale: Float = 10f,
    panLimitFactor: Float = 0.5f,  // Controls how far you can pan (0.5 = half SVG can go off-screen)
    customFont: String? = null,
) {
    val context = LocalContext.current

    val fastFontResolver = remember(context) {
        FastFontResolver(context, "fonts");
    }

    val svg = remember(svgString) {
        try {
            Log.i("SVG", "rendering SVG-data (${svgString.length / 1024} KiB)")
//            SVG.registerExternalFileResolver(SimpleAssetResolver(context.assets));
            SVG.registerExternalFileResolver(fastFontResolver);
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

        // Calculate the initial scale factor (to fit SVG in the view)
        var canvasSize by remember { mutableStateOf(Size(0f, 0f)) }

        // Track the initial scale factor to fit SVG in view
        var initialScale by remember { mutableFloatStateOf(1f) }

        Box(
            modifier = modifier
                .clipToBounds()
                // Handle pinch-zoom and pan gestures
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        // Calculate new scale with constraints
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                        // Calculate the center of the canvas
                        val canvasCenterX = canvasSize.width / 2f
                        val canvasCenterY = canvasSize.height / 2f

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

                        // Calculate maximum allowed panning in each direction
                        // Allow panning until half of the SVG is off screen (adjust divisor as needed)
                        val maxPanX = svgWidth * initialScale * panLimitFactor * newScale
                        val maxPanY = svgHeight * initialScale * panLimitFactor * newScale

                        // Update scale and offset
                        scale = newScale
                        offset = Offset(
                            (newOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                            (newOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                        )
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .graphicsLayer {
                        // Apply current scale and offset
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .onSizeChanged { size ->
                        // Update canvas size when it changes
                        canvasSize = Size(size.width.toFloat(), size.height.toFloat())

                        // Calculate initial scale to fit the SVG
                        val scaleX = size.width / svgWidth
                        val scaleY = size.height / svgHeight
                        initialScale = minOf(scaleX, scaleY) * 1.0f // add some margin if required
                    }
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawWithContent {
                            drawIntoCanvas { canvas ->
                                Log.i("Canvas", "drawIntoCanvas")
                                val nativeCanvas = canvas.nativeCanvas

//                    // Calculate initial centering
                                val viewportWidth = size.width
                                val viewportHeight = size.height

                                val centeringX = (viewportWidth - svgWidth * initialScale) / 2f
                                val centeringY = (viewportHeight - svgHeight * initialScale) / 2f

                                // Save canvas state
                                nativeCanvas.withTranslation(centeringX, centeringY) {

                                    // Apply initial centering transform
                                    scale(initialScale, initialScale)

                                    // background for SVG
                                    nativeCanvas.drawRect(
                                        0f,
                                        0f,
                                        svgWidth,
                                        svgHeight,
                                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                            color =
                                                0x08000000.toInt()
                                        })

                                    // Set up render options
                                    val renderOptions = RenderOptions()

                                    val customCss = mutableListOf<String>()

                                    // Apply tint if specified
                                    tintColor?.let {
                                        val color = String.format(
                                            "#%02X%02X%02X%02X",
                                            (it.red * 255).toInt(),
                                            (it.green * 255).toInt(),
                                            (it.blue * 255).toInt(),
                                            (it.alpha * 255).toInt()
                                        )
                                        customCss.add("svg { fill: $color;}")
                                        customCss.add("path { color: $color;}")
                                    }

                                    // use custom font for all text items
                                    customFont?.let {
                                        customCss.add("text { font-family: $it;}")
                                    }

                                    // apply all custom css settings in one operation
                                    if (customCss.size > 0) {
                                        renderOptions.css(customCss.joinToString(" "))
                                    }

                                    // Render the SVG
                                    val timeMillis = measureTimeMillis {
                                        svg.renderToCanvas(this, renderOptions)
                                    }
                                    Log.d("SVG", "renderToCanvas() took $timeMillis ms")
                                }
                            }
                        }
                    }
            ) {
                // canvas background
//                drawRect(Color.Yellow)
            }
            Text(
                text = "Scale: $scale, $offset, svgWidth: $svgWidth, svgHeight: $svgHeight, canvasSize: $canvasSize"
            )
        }
    }
}


/**
 * A cache for SVG rendered bitmaps. This helps prevent re-rendering the same SVG multiple times.
 */
/**
 * An enhanced SVG cache that supports adding bounds information for panning
 */
object SvgCache {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun get(key: String): ImageBitmap? = cache[key]

    fun put(key: String, bitmap: ImageBitmap) {
        cache[key] = bitmap
    }

    fun clear() {
        cache.clear()
    }
}

/**
 * Loads an SVG from assets and renders it to a bitmap
 *
 * @param context
 * @param assetName The name of the SVG file in the assets folder
 * @param svgString
 * @param canvasSize
 * @return The rendered bitmap as an ImageBitmap
 */
suspend fun renderToImageBitmap(
    context: Context,
    assetName: String,
    svgString: String,
    canvasSize: Size,
    fontResolver: FastFontResolver,
    tintColor: Color? = null,
    customFont: String? = null,
): ImageBitmap? = withContext(Dispatchers.IO) {
    val cacheKey = "$assetName-${canvasSize.width}-${canvasSize.height}"

    // Check if the SVG is already cached
    SvgCache.get(cacheKey)
        ?.let { return@withContext it }

    // Load and render the SVG
    val svg = try {
        Log.i("SVG", "rendering SVG-data (${svgString.length / 1024} KiB)")
        SVG.registerExternalFileResolver(fontResolver);
        SVG.getFromString(svgString)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    svg?.let {
        // Get SVG dimensions or use defaults
        val svgWidth = if (it.documentWidth != -1f) it.documentWidth else canvasSize.width
        val svgHeight = if (it.documentHeight != -1f) it.documentHeight else canvasSize.height

        // Set up render options
        val renderOptions = RenderOptions()
        val customCss = mutableListOf<String>()

        // Apply tint if specified
        tintColor?.let {
            val color = String.format(
                "#%02X%02X%02X%02X",
                (it.red * 255).toInt(),
                (it.green * 255).toInt(),
                (it.blue * 255).toInt(),
                (it.alpha * 255).toInt()
            )
            customCss.add("svg { fill: $color;}")
            customCss.add("path { color: $color;}")
        }

        // use custom font for all text items
        customFont?.let {
            customCss.add("text { font-family: $it;}")
        }

        // apply all custom css settings in one operation
        if (customCss.size > 0) {
            renderOptions.css(customCss.joinToString(" "))
        }

        val picture = Picture()
        val canvas = picture.beginRecording(svgWidth.toInt(), svgHeight.toInt())

        // Set SVG viewport
        it.renderToCanvas(canvas, renderOptions)

        picture.endRecording()

        // Convert Picture to Bitmap
        val bitmap = createBitmap(svgWidth.toInt(), svgHeight.toInt())
        val bitmapCanvas = android.graphics.Canvas(bitmap)
        bitmapCanvas.drawPicture(picture)

        // Cache the rendered bitmap
        val imageBitmap = bitmap.asImageBitmap()
        SvgCache.put(cacheKey, imageBitmap)

        return@withContext imageBitmap
    }
    return@withContext null
}

/**
 * A composable that loads and displays an SVG from assets with panning support
 */
@Composable
fun PannableCachedSvgImage(
    assetName: String,
    svgString: String,
    modifier: Modifier = Modifier,
    minScale: Float = 0.5f,
    maxScale: Float = 10f,
    panLimitFactor: Float = 0.5f,  // Controls how far you can pan (0.5 = half SVG can go off-screen)
    contentScale: ContentScale = ContentScale.Fit,
    enablePanning: Boolean = true,
    tintColor: Color? = null,
    customFont: String? = null,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Convert dp to pixels
//    val widthPx = with(density) { width.toPx().toInt() }
//    val heightPx = with(density) { height.toPx().toInt() }

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    // Get SVG dimensions or use defaults
    var svgSize by remember { mutableStateOf(Size(0f, 0f)) }

    // Calculate the initial scale factor (to fit SVG in the view)
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    var viewportSize by remember { mutableStateOf(Size(0f, 0f)) }

    // Track the initial scale factor to fit SVG in view
    var initialScale by remember { mutableFloatStateOf(1f) }

    val fastFontResolver = remember(context) {
        FastFontResolver(context, "fonts");
    }

    // Load SVG in a coroutine when the composable enters composition
    LaunchedEffect(canvasSize) {
        delay(100)

        if (canvasSize != Size.Zero) {
            Log.d("SVG", "render SVG to canvas $canvasSize")

            val timeMillis = measureTimeMillis {
                imageBitmap = renderToImageBitmap(
                    context = context,
                    assetName = assetName,
                    svgString = svgString,
                    canvasSize = canvasSize,
                    fontResolver = fastFontResolver,
                    tintColor = tintColor,
                    customFont = customFont)
                imageBitmap?.let { bitmap ->
                    svgSize = Size(bitmap.width.toFloat(), bitmap.height.toFloat())

                    // Calculate initial scale to fit the SVG
                    val scaleX = canvasSize.width / svgSize.width
                    val scaleY = canvasSize.height / svgSize.height
                    initialScale = minOf(scaleX, scaleY) * 1.0f // add some margin if required
                }
            }
            Log.d("SVG", "renderToImageBitmap() took $timeMillis ms")
        }
    }

    // Clear the bitmap when the composable leaves composition
    DisposableEffect(assetName) {
        onDispose {
            // Optional: You could remove the specific item from cache here
            // SvgCache.remove("$assetName-$widthPx-$heightPx")
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(Color.Magenta)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        offset = Offset.Zero
                        scale = 1f
                    }
                )
            }
            .pointerInput(enablePanning) {
                if (enablePanning) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        // Calculate new scale with constraints
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                        // Calculate the center of the canvas
                        val canvasCenterX = canvasSize.width / 2f
                        val canvasCenterY = canvasSize.height / 2f

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

                        // Calculate maximum allowed panning in each direction
                        // Allow panning until half of the SVG is off screen (adjust divisor as needed)
                        val maxPanX = svgSize.width * initialScale * panLimitFactor * newScale
                        val maxPanY = svgSize.height * initialScale * panLimitFactor * newScale

                        // Update scale and offset
                        scale = newScale
                        offset = Offset(
                            (newOffset.x + pan.x), //.coerceIn(-maxPanX, maxPanX),
                            (newOffset.y + pan.y) //.coerceIn(-maxPanY, maxPanY)
                        )
                    }
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
                .onSizeChanged { size ->
                    // Update canvas size when it changes
                    viewportSize = size.toSize()

                    if (canvasSize == Size.Zero) {
                        canvasSize = viewportSize
                    }
                }
                .drawWithCache {
                    onDrawWithContent {
                        // Viewport background
                        drawRect(Color.Yellow, Offset.Zero, canvasSize)

                        val centeringX = (canvasSize.width - svgSize.width * initialScale) / 2f
                        val centeringY = (canvasSize.height - svgSize.height * initialScale) / 2f

                        Log.d("Canvas","viewportSize=$viewportSize, canvasSize=$canvasSize, svgSize=$svgSize")
                        Log.d("Transform","scale=$scale, offset=$offset, initialScale=$initialScale")

                        withTransform({
                            translate(centeringX, centeringY)
                            scale(initialScale, Offset.Zero)
                        }) {
                            // SVG background (grey)
                            drawRect(Color.hsv(0f,0f, 0.95f), Offset.Zero, svgSize)

                            // actual content
                            imageBitmap?.let { bitmap ->
                                drawImage(image = bitmap)
                            }
                        }
                    }
                }
        ) {
        }

    }
}