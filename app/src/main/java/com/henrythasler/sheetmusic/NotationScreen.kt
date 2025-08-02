package com.henrythasler.sheetmusic

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

// UI state for the notation screen
sealed class EngravingState {
    data object Loading : EngravingState()
    data object Success : EngravingState()
    data class Error(val message: String) : EngravingState()
}

@Composable
fun NotationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    verovio: VerovioViewModel,
    assetPath: String,
    initialScale: Float? = null,
    initialOffset: Offset? = null,
    ) {
    val context = LocalContext.current
    var loadingState by remember { mutableStateOf<EngravingState>(EngravingState.Loading) }
    var engravingState by remember { mutableStateOf<EngravingState>(EngravingState.Loading) }
    var svgDocument by remember { mutableStateOf<String?>(null) }
    var timemap by remember { mutableStateOf<Array<TimemapItem>>(emptyArray()) }
    var engraveTimeMillis by remember { mutableLongStateOf(0L) }
    var currentPage by remember { mutableIntStateOf(1) }
    var pageCount by remember { mutableIntStateOf(1) }

    val settings = useSettings()
    val selectedCustomFont = settings.svgFontOverride.collectAsState(initial = null).value
    val customFont: CustomFont? = remember(selectedCustomFont) {
        SvgCustomFonts[selectedCustomFont]
    }

    val assetName = assetPath.substringAfterLast("/")

    fun rescaleFont(input: String, scale: Float): String {
        val regex = Regex("(?<=font-size=\")\\d+(?=[a-z]*\")")
        val matches = regex.findAll(input).toList()
        val output = StringBuilder()

        var lastIndex = 0

        for(match in matches) {
            // Add text before the match
            output.append(input.substring(lastIndex, match.range.first))

            // Add the replacement
            val fontSize = match.value.toFloat()
            val replacement = "${(fontSize * scale).toInt()}"
            output.append(replacement)

            lastIndex = match.range.last + 1
        }
        // Add remaining text after last match
        output.append(input.substring(lastIndex))
        return output.toString()
    }

    LaunchedEffect(Unit) {
        engraveTimeMillis = measureTimeMillis {
            if(verovio.loadMusicAsset(
                    context = context,
                    assetPath = assetPath,
                    lyricSizeScale = settings.vrvLyricSizeScale.first(),
                    lyricWordSpaceScale = settings.vrvLyricWordSpaceScale.first(),
            )) {
                loadingState = EngravingState.Success

                svgDocument = verovio.engravePage(currentPage, false)
//                svgDocument = svgDocument?.replace(Regex("(?<=font-family:).*?(?=;)"), "LiberationSerif-Regular")
//                Log.d("SVG", "$svgDocument")

                timemap = verovio.getTimemap() ?: emptyArray<TimemapItem>()
                val last = timemap.takeLast(3)
                Log.i("Verovio", "Timemap (${timemap.size} Items): $last")
                pageCount = verovio.getPageCount()

                engravingState = if(svgDocument.isNullOrEmpty()) {
                    EngravingState.Error("Loading failed!")
                }
                else {
                    EngravingState.Success
//                svgDocument = svgDocument?.replace(Regex("(?<=font-family:).*?(?=;)"), "OpenSans")

    //            svgDocument?.let { doc ->
    //                val scale = settings.svgFontScale.first()
    //                svgDocument = rescaleFont(doc, scale)
    //            }
                }
            }
            else {
                loadingState = EngravingState.Error("Loading failed!")
            }
        }
        Log.i("Verovio", "Engraving '$assetPath' took $engraveTimeMillis ms. (${svgDocument?.length} Bytes)")
    }

    LaunchedEffect(currentPage) {
        if(loadingState == EngravingState.Success) {
            svgDocument = verovio.engravePage(currentPage, false)
            engravingState = if(svgDocument.isNullOrEmpty()) {
                EngravingState.Error("Engraving failed!")
            }
            else {
                EngravingState.Success
            }
        }
    }

    val coroutineScope = remember { CoroutineScope(Dispatchers.IO) }

    val exportSvg = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri: Uri? ->
        uri?.let {
            Log.i("SVG", "saving track to: ${it.path}")
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                        outputStream.write(svgDocument?.toByteArray())
                    }
                } catch (e: Exception) {
                    Log.e("SVG", "Error saving SVG file $uri: $e")
                }
            }
        }
    }

    Column {
        NotationTopNavigationBar(
            title = assetName,
            onNavigateBack = onNavigateBack,
            onNavigateToSettings = onNavigateToSettings,
            onExportSvg = {
                exportSvg.launch("$assetName.svg")
            },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (engravingState) {
                is EngravingState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is EngravingState.Success -> {
                    svgDocument?.let { svg ->
                        ScalableCachedSvgImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            title = "Engrave: $engraveTimeMillis ms (${svg.length / 1024} KiB)",
                            svgDocument = svg,
                            timemap = timemap,
                            svgConfig = SvgConfig(
                                null,
                                customFont
                            ),
                            canvasConfig = CanvasConfig(
                                minScale = 0.25f,
                                maxScale = 32f,
                                debounceDelayMs = 100L,
                                panLimit = 0f,
                                canvasExtension = 800f
                            ),
                            initialScale = settings.currentScale,
                            initialOffset = settings.currentOffset,
                        )
                    }

                    if(currentPage > 1) {
                        FloatingActionButton(
                            modifier = Modifier.align(Alignment.CenterStart),
                            onClick = {
                                currentPage = max(currentPage - 1, 1)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_navigate_before_24),
                                contentDescription = null
                            )
                        }
                    }

                    if(currentPage < pageCount) {
                        FloatingActionButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            onClick = {
                                currentPage = min(currentPage + 1, pageCount)
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_navigate_next_24),
                                contentDescription = null
                            )
                        }
                    }
                }

                is EngravingState.Error -> {
                    val errorMessage = (engravingState as EngravingState.Error).message
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    widthDp = 400,
    heightDp = 64,
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotationTopNavigationBar(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onExportSvg: () -> Unit = {},
    title: String = "Title",
) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.windowInsetsPadding(WindowInsets.displayCutout),
                onClick = {
                    onNavigateBack()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            NotationActionMenu(
                onNavigateToSettings = onNavigateToSettings,
                onExportSvg = onExportSvg,
            )
        },
    )
}


@Composable
fun NotationActionMenu(
    onNavigateToSettings: () -> Unit = {},
    onExportSvg: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }
    val settings = useSettings()

    Row {
        IconButton(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.displayCutout),
            onClick = {
                onNavigateToSettings()
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_settings_24),
                contentDescription = null
            )
        }

        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Export SVG...") },
                leadingIcon = { Icon(painterResource(R.drawable.rounded_download_24), contentDescription = null) },
                onClick = {
                    expanded = false
                    onExportSvg()
                }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = {
                    expanded = false
                    openAlertDialog.value = true
                }
            )
        }
    }

    if (openAlertDialog.value) {
        AboutDialog(openAlertDialog, settings.verovioVersion)
    }
}