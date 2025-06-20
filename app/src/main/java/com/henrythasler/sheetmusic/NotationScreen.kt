package com.henrythasler.sheetmusic

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlin.system.measureTimeMillis

@Composable
fun NotationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    engraveMusicAsset: suspend(context: Context, assetPath: String) -> String?,
    assetPath: String,
) {
    val context = LocalContext.current
    var svgDocument by remember { mutableStateOf<String?>(null) }
    var engraveTimeMillis by remember { mutableLongStateOf(0L) }

    val settings = useSettings()
    val svgOverrideFont by settings.svgOverrideFont.collectAsState(initial = null)

    val assetName = assetPath.substringAfterLast("/")

    LaunchedEffect(Unit) {
        engraveTimeMillis = measureTimeMillis {
            svgDocument = engraveMusicAsset(context, assetPath)
        }
        Log.i("Verovio", "Engraving '$assetPath' took $engraveTimeMillis ms. (${svgDocument?.length} Bytes)")
    }

    Column {

        NotationTopNavigationBar(
            onNavigateBack = onNavigateBack,
            onNavigateToSettings = onNavigateToSettings,
            title = assetName
            )

        svgDocument?.let { svg ->
            ScalableCachedSvgImage(
                modifier = Modifier
                    .fillMaxSize(),
                title = "Engrave: $engraveTimeMillis ms (${svg.length / 1024} KiB)",
                svgDocument = svg,
                svgConfig = SvgConfig(
                    null,
                    if (svgOverrideFont != "off") svgOverrideFont else null
                ),
                canvasConfig = CanvasConfig(
                    minScale = 0.25f,
                    maxScale = 32f,
                    debounceDelayMs = 100L,
                    panLimit = 0f,
                    canvasExtension = 800f
                ),
            )
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
    title: String = "Title"
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
            )
        },
    )
}


@Composable
fun NotationActionMenu(
    onNavigateToSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }

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
        AboutDialog(openAlertDialog)
    }
}