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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.createBitmap
import com.caverock.androidsvg.RenderOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGExternalFileResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * Configuration for dynamic resolution rendering
 */
data class CanvasConfig(
    val minScale: Float = 0.1f,
    val maxScale: Float = 10f,
    val debounceDelayMs: Long = 300L, // Delay before rendering higher resolution
    val panLimit: Float = 0.5f,
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
 * Loads an SVG and renders it at a specific scale factor
 */
suspend fun imageBitmapFromSvgAtScale(
    svgString: String = "",
    canvasSize: Size = Size.Zero,
    offset: Offset = Offset.Zero,
    scale: Float = 1.0f,
    svgConfig: SvgConfig = SvgConfig(),
    fontResolver: FastFontResolver,
): ImageBitmap? = withContext(Dispatchers.IO) {

    // Load and render the SVG
    // FIXME: cache svg object
    val svg = try {
        Log.i("SVG", "rendering SVG-data (${svgString.length / 1024} KiB)")
        SVG.registerExternalFileResolver(fontResolver);
        SVG.getFromString(svgString)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    svg?.let {
        val svgSize = Size(
            if (it.documentWidth != -1f) it.documentWidth else canvasSize.width,
            if (it.documentHeight != -1f) it.documentHeight else canvasSize.height
        )
        Log.d("SVG", "imageBitmapFromSvgAtScale(): svgSize: $svgSize")

        val picture = Picture()
        val canvas = picture.beginRecording(canvasSize.width.toInt(), canvasSize.height.toInt())

        val scaleX = canvasSize.width / svgSize.width
        val scaleY = canvasSize.height / svgSize.height
        val initialScale =
            minOf(scaleX, scaleY) * 1.0f // add some margin if required

        val centeringX = (canvasSize.width - svgSize.width * initialScale) / 2f
        val centeringY = (canvasSize.height - svgSize.height * initialScale) / 2f


        Log.d("SVG", "imageBitmapFromSvgAtScale(): $scale, $offset, Center=$centeringX, $centeringY, initialScale=$initialScale")
        canvas.translate(offset.x, offset.y)
        canvas.scale(scale, scale, canvasSize.width / 2f, canvasSize.height / 2f)

        // SVG background
        val paint = Paint()
        paint.color = 0xffffff
        paint.alpha = 200
        canvas.drawRect(
            centeringX,
            centeringY,
            centeringX + svgSize.width * initialScale,
            centeringY + svgSize.height * initialScale,
            paint
        )

        // Set up render options
        val renderOptions = RenderOptions()
        val customCss = mutableListOf<String>()

        // Apply tint if specified
        svgConfig.tintColor?.let { tintColor ->
            val color = String.format(
                "#%02X%02X%02X%02X",
                (tintColor.red * 255).toInt(),
                (tintColor.green * 255).toInt(),
                (tintColor.blue * 255).toInt(),
                (tintColor.alpha * 255).toInt()
            )
            customCss.add("svg { fill: $color;}")
            customCss.add("path { color: $color;}")
        }

        // use custom font for all text items
        svgConfig.customFont?.let { font ->
            customCss.add("text { font-family: $font;}")
        }

        // apply all custom css settings in one operation
        if (customCss.size > 0) {
            renderOptions.css(customCss.joinToString(" "))
        }

        // Set SVG viewport to the target resolution
        it.setDocumentWidth(canvasSize.width)
        it.setDocumentHeight(canvasSize.height)
        it.renderToCanvas(canvas, renderOptions)

        picture.endRecording()

        // Convert Picture to Bitmap
        val bitmap = createBitmap(
            canvasSize.width.toInt(),
            canvasSize.height.toInt(),
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
) {
    val context = LocalContext.current
    val coroutineScope = remember { CoroutineScope(Dispatchers.Main) }

    var baseSize by remember { mutableStateOf<Size?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var viewportSize by remember { mutableStateOf(Size.Zero) }

    // State for transformations
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    var renderOffset by remember { mutableStateOf(Offset.Zero) }
    var renderScale by remember { mutableFloatStateOf(1f) }

    // Current bitmap and render job
    var currentBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var renderJob by remember { mutableStateOf<Job?>(null) }
    var renderTime by remember { mutableLongStateOf(0L) }


    val fastFontResolver = remember(context) {
        FastFontResolver(context, "fonts");
    }

    // Re-render with debouncing
    LaunchedEffect(renderOffset, renderScale) {
        renderJob?.cancel()

        // Start new render job with debounce
        renderJob = coroutineScope.launch {
            delay(canvasConfig.debounceDelayMs)

            renderTime = measureTimeMillis {
                currentBitmap = imageBitmapFromSvgAtScale(
                    svgString = svgDocument,
                    canvasSize = canvasSize,
                    offset = renderOffset,
                    scale = renderScale,
                    svgConfig = svgConfig,
                    fontResolver = fastFontResolver
                )
            }

            // reset viewport transformation as the new bitmap already includes all transformations
            scale = 1f
            offset = Offset.Zero
            Log.d("Canvas", "imageBitmapFromSvgAtScale: ${currentBitmap?.width}x${currentBitmap?.height} took $renderTime ms")
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
            .background(Color.Magenta)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // reset all transformations
                        offset = Offset.Zero
                        scale = 1f

                        renderScale = 1f
                        renderOffset = Offset.Zero
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // Calculate new scale with constraints
                    val newScale =
                        (scale * zoom).coerceIn(canvasConfig.minScale, canvasConfig.maxScale)

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
                        (newOffset.x + pan.x), //.coerceIn(-maxPanX, maxPanX),
                        (newOffset.y + pan.y), //.coerceIn(-maxPanY, maxPanY)
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
                    renderScale = newRenderScale
                    renderOffset = Offset(
                        (newRenderOffset.x + pan.x), //.coerceIn(-maxPanX, maxPanX),
                        (newRenderOffset.y + pan.y), //.coerceIn(-maxPanY, maxPanY)
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
                        drawRect(Color.Yellow, Offset.Zero, viewportSize)

                        currentBitmap?.let { bitmap ->
                            val centeringX = (canvasSize.width - bitmap.width) / 2f
                            val centeringY = (canvasSize.height - bitmap.height) / 2f
                            Log.d("Canvas", "canvas: $canvasSize, bitmap: ${bitmap.width}x${bitmap.height}, center: ($centeringX, $centeringY)")

                            withTransform({
                                translate(centeringX, centeringY)
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
                .background(Color.hsv(0f, 0f, 1f, 0.5f)),
        ) {
            Text(
                modifier = Modifier
                    .padding(6.dp),
                text = "$title\nViewport: $scale, $offset\nCanvas: $renderScale, $renderOffset\nRender: $renderTime ms\nBitmap: ${currentBitmap?.width}x${currentBitmap?.height}"
            )
        }
    }
}