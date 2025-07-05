package com.henrythasler.sheetmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(
    backgroundColor = 0xFFFFFFFF,
    showBackground = true,
)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
) {
    val settings = useSettings()

    val svgFontScale = settings.svgFontScale.collectAsState(initial = 0.8).value
    var fontScale = remember(svgFontScale) {
        svgFontScale.toFloat()
    }

    Scaffold(
        topBar = {
            SettingsTopNavigationBar(
                title = "Render Settings",
                onNavigateBack = onNavigateBack,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
//                .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {

                    Text(
                        modifier = Modifier
                            .padding(bottom = 16.dp),
                        text = "SVG Font Override",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    AdvancedFontPickerDropdown()
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 16.dp),
                        text = "SVG Font Scale",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Slider(
                        value = fontScale,
                        steps = 7,
                        valueRange = 0.4f..1.2f,
                        onValueChange = {
                            fontScale = it
                            settings.updateSvgFontScale(it)
                        }
                    )
                    Text(text = "%.1f".format(fontScale))
                }
            }

            Card(
                modifier = Modifier
//                .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 16.dp),
                        text = "SVG Rendering Resolution",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    BitmapQualitySelector()
                }
            }
        }
    }
}

@Preview(
    widthDp = 300,
    heightDp = 64,
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopNavigationBar(
    onNavigateBack: () -> Unit = {},
    title: String = "Title"
) {
    TopAppBar(
//        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
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
                    maxLines = 1
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
        }
    )
}

@Composable
fun BitmapQualitySelector(modifier: Modifier = Modifier){
    val settings = useSettings()
    val svgRenderResolution by settings.svgRenderResolution.collectAsState(initial = SvgRenderResolutionEnum.HIGH)
    val radioOptions = mapOf(
        "High" to SvgRenderResolutionEnum.HIGH,
        "Medium" to SvgRenderResolutionEnum.MEDIUM,
        "Low" to SvgRenderResolutionEnum.LOW,
        )

    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
    Column(modifier.selectableGroup()) {
        radioOptions.forEach { (name, value) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .selectable(
                        selected = (value == svgRenderResolution),
                        onClick = { settings.updateSvgRenderResolution( value) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (value == svgRenderResolution),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}


class CanvasTextPainter(
    private val text: String,
    private val textStyle: TextStyle,
    private val density: Density,
    private val fontFamilyResolver: FontFamily.Resolver,
    private val size: androidx.compose.ui.geometry.Size
) : Painter() {

    override val intrinsicSize: androidx.compose.ui.geometry.Size = size

    override fun DrawScope.onDraw() {
        val textMeasurer = TextMeasurer(
            defaultFontFamilyResolver = fontFamilyResolver,
            defaultDensity = Density(density),
            defaultLayoutDirection = LayoutDirection.Ltr
        )
        val textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(text),
            style = textStyle,
        )

        // Center the text
        val offsetX = (size.width - textLayoutResult.size.width) / 2
        val offsetY = (size.height - textLayoutResult.size.height) / 2

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(offsetX, offsetY)
        )
    }
}

@Composable
fun rememberCanvasTextPainter(
    text: String,
    size: Dp = 24.dp,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontFamily: FontFamily? = null // Add custom font family parameter
): Painter {
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val sizeInPx = with(density) { size.toPx() }
    val resolvedStyle = style.copy(
        color = if (color != Color.Unspecified) color else style.color,
        fontSize = (size.value * 0.6).sp, // Adjust font size relative to icon size
        fontFamily = fontFamily ?: style.fontFamily // Use custom font if provided
    )

    return remember(text, size, resolvedStyle, density) {
        CanvasTextPainter(
            text = text,
            textStyle = resolvedStyle,
            density = density,
            fontFamilyResolver = fontFamilyResolver,
            size = androidx.compose.ui.geometry.Size(sizeInPx, sizeInPx)
        )
    }
}

// Helper function to create FontFamily from TTF in assets
@Composable
fun createFontFamilyFromAssets(fontPath: String): FontFamily {
    return FontFamily(
        Font(
            path = fontPath,
            assetManager = LocalContext.current.assets,
//            weight = FontWeight.Normal,
//            style = FontStyle.Normal,
        )
    )
}

// More advanced version with custom fonts
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFontPickerDropdown() {

    var expanded by remember { mutableStateOf(false) }

    val settings = useSettings()
    val selectedCustomFont = settings.svgFontOverride.collectAsState(initial = SvgCustomFonts.keys.first()).value
    val customFont: CustomFont = remember(selectedCustomFont) {
        SvgCustomFonts[selectedCustomFont] ?: SvgCustomFonts.values.first()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = customFont.description,
            onValueChange = { },
            readOnly = true,
//            label = { Text("Select Font") },
            supportingText = {
                Text("Set the font that will be used to override music document font definitions.")
            },
            leadingIcon = {
                Icon(
                    painter = rememberCanvasTextPainter(
                        text = "Ag",
                        size = 48.dp,
                        fontFamily = customFont.fontFamily ?: FontFamily.Serif,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    contentDescription = "Font preview",
                    modifier = Modifier.size(48.dp)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SvgCustomFonts.forEach { (key, customFont) ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = customFont.description,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "The quick brown fox jumps",
                                fontFamily = customFont.fontFamily ?: FontFamily.Serif,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = rememberCanvasTextPainter(
                                text = "Ag",
                                size = 48.dp,
                                fontFamily = customFont.fontFamily ?: FontFamily.Serif,
                                color = MaterialTheme.colorScheme.secondary
                            ),
                            contentDescription = "Font preview",
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    onClick = {
                        settings.updateSvgFontOverride(key)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}