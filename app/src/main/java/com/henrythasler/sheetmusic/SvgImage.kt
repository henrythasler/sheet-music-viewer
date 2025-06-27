package com.henrythasler.sheetmusic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.Typeface
import android.util.Log
import androidx.collection.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.createBitmap
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGExternalFileResolver
import com.caverock.androidsvg.SVGParseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

/**
 * Configuration for dynamic resolution rendering
 */
data class CanvasConfig(
    val minScale: Float = 0.1f,
    val maxScale: Float = 10f,
    val debounceDelayMs: Long = 300L, // Delay before rendering higher resolution
    val panLimit: Float = 0.5f,

    // add some extra space (pixels) around the SVG to allow for panning
    // Ensure canvas size is at least as large as the SVG size, with some extra space for panning
    val canvasExtension: Float = 800f,
)

data class SvgConfig(
    val tintColor: Color? = null,
    val customFont: String? = null,
)


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
//            Log.d("FastFontResolver", "cache hit for $fontFamily ($fontWeight, $fontStyle)")
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

class SVGCache {
    companion object {
        private val svgCache = LruCache<Int, SVG>(4)

        fun getCachedSVG(svgString: String): SVG? {
            val key = svgString.hashCode()
            return svgCache[key] ?: run {
                try {
                    Log.i("SVG", "parsing SVG-data (${svgString.length / 1024} KiB)")
                    val svg = SVG.getFromString(svgString)
                    svgCache.put(key, svg)
                    svg
                } catch (e: SVGParseException) {
                    Log.e("SVG", "Failed to parse SVG", e)
                    null
                }
            }
        }
    }
}

/**
 * Loads an SVG and renders it at a specific scale factor
 */
suspend fun imageBitmapFromSvgAtScale(
    svgString: String = "",
    canvasSize: Size = Size.Zero,
    offset: Offset = Offset.Zero,
    scale: Float = 1.0f,
    bitmapScale: Float = 1.0f,
    svgConfig: SvgConfig = SvgConfig(),
    canvasConfig: CanvasConfig = CanvasConfig(),
    fontResolver: FastFontResolver,
): ImageBitmap? = withContext(Dispatchers.IO) {

    // Load and render the SVG
    SVGCache.getCachedSVG(svgString)?.let { svg ->
        // get SVG size from the document, or use the canvas size if not specified
        val svgSize = Size(
            if (svg.documentWidth != -1f) svg.documentWidth else canvasSize.width,
            if (svg.documentHeight != -1f) svg.documentHeight else canvasSize.height
        )
        Log.d("SVG", "imageBitmapFromSvgAtScale(): svgSize: $svgSize")

        // Set up AndroidSVG render options
        val renderOptions = RenderOptions()

        // to collect custom CSS styles that will be applied to the SVG
        val customCss = mutableListOf<String>()

        // Apply tint if specified
        svgConfig.tintColor?.let { tintColor ->
            val color = "#%02X%02X%02X%02X".format(
                (tintColor.red * 255).toInt(),
                (tintColor.green * 255).toInt(),
                (tintColor.blue * 255).toInt(),
                (tintColor.alpha * 255).toInt()
            )
            customCss.add("svg { fill: $color; }")
            customCss.add("path { color: $color; }")
        }

        // use custom font for all text items
        SVG.registerExternalFileResolver(fontResolver);
        svgConfig.customFont?.let { font ->
            Log.d("SVG", "Using custom font: '$font'")
            customCss.add("text { font-family: $font; }")
        }

        // merge and apply custom CSS styles
        if (customCss.size > 0) {
            renderOptions.css(customCss.joinToString(" "))
        }

        // create a Picture to draw the SVG into
        // this allows us to render the SVG at a specific scale and offset
        // and then convert it to a Bitmap
        // this is required because AndroidSVG does not support scaling and offsetting directly
        val picture = Picture()
        val canvas = picture.beginRecording(
            ((canvasSize.width + 2 * canvasConfig.canvasExtension) * bitmapScale).toInt(),
            ((canvasSize.height + 2 * canvasConfig.canvasExtension) * bitmapScale).toInt()
            )

        // Calculate initial scale to fit and center the SVG into the canvas
        val scaleX = canvasSize.width / svgSize.width
        val scaleY = canvasSize.height / svgSize.height

        // maintains aspect ratio while scaling to fit the canvas
        val initialScale =
            minOf(scaleX, scaleY) * 1.0f * bitmapScale // add some margin if required

        val centeringX = (canvas.width - svgSize.width) / 2f
        val centeringY = (canvas.height - svgSize.height) / 2f

        Log.d(
            "SVG",
            "imageBitmapFromSvgAtScale(): $scale, ${offset * bitmapScale}, Center=$centeringX, $centeringY, initialScale=$initialScale"
        )

        // Bitmap background for debugging
        val background = Paint()
        background.color = 0x00ff00
        background.alpha = 0x80
//        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), background)

        
        // Apply the offset and scale to the canvas
        // This allows us to pan and zoom the SVG
        canvas.translate(offset.x * bitmapScale, offset.y * bitmapScale)
        canvas.scale(scale, scale, canvas.width / 2f, canvas.height / 2f)

        // Center and fit the SVG in the canvas        
        canvas.translate(centeringX, centeringY)
        canvas.scale(initialScale, initialScale, svg.documentWidth / 2f, svg.documentHeight / 2f)

        // SVG background
        val paint = Paint()
        paint.color = Color.White.toArgb()
        canvas.drawRect(0f, 0f, svgSize.width, svgSize.height, paint)

        // Render the SVG to the canvas using the render options
        svg.renderToCanvas(canvas, renderOptions)

        picture.endRecording()

        // Convert Picture to Bitmap
        val bitmap = createBitmap(
            canvas.width,
            canvas.height,
            Bitmap.Config.ARGB_8888
        )
        val bitmapCanvas = android.graphics.Canvas(bitmap)
        bitmapCanvas.drawPicture(picture)

        // Cache the rendered bitmap
        val imageBitmap = bitmap.asImageBitmap()

        return@withContext imageBitmap
    }
    return@withContext null
}


/**
 * A composable that loads and displays an SVG from assets with panning support
 */
@Composable
fun ScalableCachedSvgImage(
    title: String,
    svgDocument: String,
    modifier: Modifier = Modifier,
    canvasConfig: CanvasConfig = CanvasConfig(),
    svgConfig: SvgConfig = SvgConfig(),
    initialScale: Float = 1.0f,
    initialOffset: Offset = Offset.Zero,
) {
    val context = LocalContext.current
    val coroutineScope = remember { CoroutineScope(Dispatchers.Main) }

    var baseSize by remember { mutableStateOf<Size?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var viewportSize by remember { mutableStateOf(Size.Zero) }

    // State for transformations
    var offset by remember { mutableStateOf(initialOffset) }
    var scale by remember { mutableFloatStateOf(initialScale) }

    var renderOffset by remember { mutableStateOf(initialOffset) }
    var renderScale by remember { mutableFloatStateOf(initialScale) }

    var overviewOffset by remember { mutableStateOf(Offset.Zero) }
    var overviewScale by remember { mutableFloatStateOf(1f) }

    var minScaleCurrent by remember { mutableFloatStateOf(canvasConfig.minScale) }
    var maxScaleCurrent by remember { mutableFloatStateOf(canvasConfig.maxScale) }

    // Current bitmap and render job
    var currentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var overviewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var renderJob by remember { mutableStateOf<Job?>(null) }
    var renderTime by remember { mutableLongStateOf(0L) }

    val settings = useSettings()
    val bitmapScale = SvgRenderResolutionMapping[settings.svgRenderResolution.collectAsState(initial = null).value] ?: 1f

    val fastFontResolver = remember(context) {
        FastFontResolver(context, "fonts");
    }

    // Re-render with debouncing
    LaunchedEffect(renderOffset, renderScale, bitmapScale) {
        renderJob?.cancelAndJoin()

        // Start new render job with debounce
        renderJob = coroutineScope.launch {
            delay(canvasConfig.debounceDelayMs)

            // measuring render time for benchmarking
            renderTime = measureTimeMillis {
                currentBitmap = imageBitmapFromSvgAtScale(
                    svgString = svgDocument,
                    canvasSize = canvasSize,
                    offset = renderOffset,
                    scale = renderScale,
                    bitmapScale = bitmapScale,
                    svgConfig = svgConfig,
                    canvasConfig = canvasConfig,
                    fontResolver = fastFontResolver
                )
                if(overviewBitmap == null) {
                    overviewBitmap = if (renderScale.equals(1.0f) && renderOffset == Offset.Zero) {
                        currentBitmap
                    } else {
                        imageBitmapFromSvgAtScale(
                            svgString = svgDocument,
                            canvasSize = canvasSize,
                            offset = Offset.Zero,
                            scale = 1.0f,
                            bitmapScale = bitmapScale,
                            svgConfig = svgConfig,
                            canvasConfig = canvasConfig,
                            fontResolver = fastFontResolver
                        )
                    }
                }
            }

            overviewOffset = renderOffset
            overviewScale = renderScale

            // reset viewport transformation as the new bitmap already includes all transformations
            scale = 1f
            offset = Offset.Zero

            // calculate new limits for canvas scaling from current renderScale
            minScaleCurrent = canvasConfig.minScale / renderScale
            maxScaleCurrent = canvasConfig.maxScale / renderScale

            Log.d(
                "Canvas",
                "imageBitmapFromSvgAtScale: ${currentBitmap?.width}x${currentBitmap?.height} took $renderTime ms"
            )
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            renderJob?.cancel()
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(Color.Magenta.copy(alpha = 0.1f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // reset transformations
                        renderScale = 1f
                        renderOffset = Offset.Zero
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // Calculate new scale with constraints
                    val newScale = (scale * zoom).coerceIn(minScaleCurrent, maxScaleCurrent)

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
//                    val maxPanX = canvasSize.width * canvasConfig.panLimit * newScale
//                    val maxPanY = canvasSize.height * canvasConfig.panLimit * newScale

                    // Update scale and offset
                    scale = newScale
                    offset = Offset(
                        (newOffset.x + pan.x),//.coerceIn(-maxPanX, maxPanX),
                        (newOffset.y + pan.y)//.coerceIn(-maxPanY, maxPanY)
                    )

                    // Calculate render values
                    val newRenderScale =
                        (renderScale * zoom).coerceIn(canvasConfig.minScale, canvasConfig.maxScale)
                    val renderFocusX = relativeX - renderOffset.x
                    val renderFocusY = relativeY - renderOffset.y
                    val newRenderOffset = Offset(
                        renderOffset.x + renderFocusX - renderFocusX * (newRenderScale / renderScale),
                        renderOffset.y + renderFocusY - renderFocusY * (newRenderScale / renderScale)
                    )

//                    val maxRenderPanX = canvasSize.width * canvasConfig.panLimit * newRenderScale
//                    val maxRenderPanY = canvasSize.height * canvasConfig.panLimit * newRenderScale

                    renderScale = newRenderScale
                    renderOffset = Offset(
                        (newRenderOffset.x + pan.x), //.coerceIn(-maxRenderPanX, maxRenderPanX),
                        (newRenderOffset.y + pan.y)  //.coerceIn(-maxRenderPanY, maxRenderPanY)
                    )

                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Apply scale and offset for panning and zooming (low-latency operation)
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .onSizeChanged { size ->
                    viewportSize = size.toSize()
                    canvasSize = viewportSize
                }
                .drawWithCache {
                    onDrawWithContent {
                        // Viewport background
                        drawRect(Color.Yellow.copy(alpha = 0.3f), Offset.Zero, viewportSize)

                        overviewBitmap?.let { overview ->
                            val centeringX = (canvasSize.width - overview.width) / 2f
                            val centeringY = (canvasSize.height - overview.height) / 2f
                            withTransform({
                                translate(centeringX + overviewOffset.x, centeringY + overviewOffset.y)
                                scale(
                                    1 / bitmapScale * overviewScale,
                                    1 / bitmapScale * overviewScale,
                                    Offset(overview.width / 2f, overview.height / 2f)
                                )
                            }) {
                                drawImage(image = overview)
                            }
                        }

                        currentBitmap?.let { bitmap ->
                            val centeringX = (canvasSize.width - bitmap.width) / 2f
                            val centeringY = (canvasSize.height - bitmap.height) / 2f
                            Log.d(
                                "Canvas",
                                "canvas: $canvasSize, bitmap: ${bitmap.width}x${bitmap.height}, center: ($centeringX, $centeringY)"
                            )

                            withTransform({
                                translate(centeringX, centeringY)
                                scale(
                                    1 / bitmapScale,
                                    1 / bitmapScale,
                                    Offset(bitmap.width / 2f, bitmap.height / 2f)
                                )
                            }) {
                                drawImage(image = bitmap)
                            }
                        }
                    }
                }
        ) {
        }
        Column(
            modifier = Modifier
//                .paddingFromBaseline(top = 200.dp)
                .background(Color.White.copy(alpha = 0.5f)),
        ) {
            Text(
                modifier = Modifier
                    .padding(6.dp),
                text = "$title\nViewport: $scale, $offset\nCanvas: $renderScale, $renderOffset\nOverview: $overviewScale, $overviewOffset\nBitmap: ${"%.0f".format(bitmapScale * 100)}% (${currentBitmap?.width}x${currentBitmap?.height}) ($renderTime ms)"
            )
        }
    }
}