package com.henrythasler.sheetmusic

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// Asset item class representing a file in the assets folder
data class AssetItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long?,
)

// File type enum for icon selection
enum class AssetFileType {
    //    FOLDER,
    MEI,
    OTHER
}

// Utility class for asset operations
object AssetUtils {
    fun getFileType(fileName: String): AssetFileType {
        // Check if it's a directory - this requires special handling in assets
//        if (fileName.isNotEmpty()) return AssetFileType.FOLDER

        return when (fileName.substringAfterLast(".", "").lowercase()) {
//            "xml", "musicxml" -> AssetFileType.MUSICXML
            "mei" -> AssetFileType.MEI
            else -> AssetFileType.OTHER
        }
    }

    // List all files in an assets folder
    suspend fun listAssetsInPath(context: Context, path: String = ""): List<AssetItem> {
        return withContext(Dispatchers.IO) {
            try {
                val assetManager = context.assets
                val fileList = mutableListOf<AssetItem>(AssetItem("..", path.substringBeforeLast('/'), true, null))

                // Get all files in the specified path
                val files = assetManager.list(path) ?: emptyArray()

                for (file in files) {
                    val fullPath = if (path.isEmpty()) file else "$path/$file"

                    // Check if it's a directory by seeing if it contains files
                    val isDirectory = assetManager.list(fullPath)?.isNotEmpty() ?: false

                    var fileSize: Long? = null

                    if(!isDirectory) {
                        assetManager.open(fullPath).use { inputStream ->
                            fileSize = inputStream.available().toLong()
                        }
                    }

                    fileList.add(
                        AssetItem(
                            name = file,
                            path = fullPath,
                            isDirectory = isDirectory,
                            size = fileSize,
                        )
                    )
                }

                // Sort alphabetically
//                fileList.sortedBy { !it.isDirectory }
                fileList.sortedBy { it.name.lowercase() }
            } catch (e: Exception) {
                Log.e("AssetUtils", "Error listing assets: ${e.message}")
                emptyList()
            }
        }
    }
}

// UI state for asset screen
sealed class AssetUiState {
    data object Loading : AssetUiState()
    data object Empty : AssetUiState()
    data class Success(val assets: List<AssetItem>) : AssetUiState()
    data class Error(val message: String) : AssetUiState()
}

@Composable
fun BrowserScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToNotation: (fileName: String) -> Unit = {},
    meiAssetsFolder: String? = null,
    uiState: AssetUiState,
    readAssets: suspend(context: Context, newFolder: String?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Load assets when composable is first launched
    LaunchedEffect(meiAssetsFolder) {
        readAssets(context, meiAssetsFolder)
    }

    Scaffold(
        topBar = {
            BrowserTopNavigationBar(
                onNavigateBack = onNavigateBack,
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is AssetUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is AssetUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No files found in this assets folder")
                    }
                }

                is AssetUiState.Success -> {
                    val assets = uiState.assets
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 96.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(assets) { assetItem ->
                            AssetGridItem(
                                assetItem = assetItem,
                                onClick = {
                                    // Handle asset click
                                    val message = "Selected: ${assetItem.name} (${assetItem.path})"
                                    Log.d("AssetsGridScreen", message)

                                    if (assetItem.isDirectory) {
                                        coroutineScope.launch {
                                            readAssets(context, assetItem.path)
                                        }
                                    } else {
                                        onNavigateToNotation(assetItem.path)
                                    }

                                    // Here you can handle the asset click event
                                    // Open the asset, navigate to the folder, etc.
                                }
                            )
                        }
                    }
                }

                is AssetUiState.Error -> {
                    val errorMessage = (uiState as AssetUiState.Error).message
                    Column(
                        modifier = Modifier.align(Alignment.Center),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserTopNavigationBar(
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Column {
                Text(
                    text = "Example Browser",
                    style = MaterialTheme.typography.titleLarge
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
            BrowserActionMenu()
        },
    )
}

@Composable
fun BrowserActionMenu() {
    var expanded by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }
    val settings = useSettings()

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

    if (openAlertDialog.value) {
        AboutDialog(openAlertDialog, settings.verovioVersion)
    }
}

@Composable
fun SquareCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = CardDefaults.cardElevation(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    // Using aspectRatio(1f) ensures the element remains square
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .background(backgroundColor),
        elevation = elevation,
    ) {
        content()
    }
}

@Composable
fun AssetGridItem(
    assetItem: AssetItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    SquareCard(
        modifier = Modifier
            .padding(4.dp)
//            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxHeight()
                .fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize(),
//                    .size(64.dp),
//                    .background(Color.LightGray.copy(alpha = 0.2f))
//                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f))
//                contentAlignment = Alignment.Center,

//            ) {
                if (assetItem.isDirectory) {
                    Icon(
                        painter = painterResource(R.drawable.outline_folder_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .weight(1f)
                            .align(Alignment.CenterHorizontally),
//                        tint = Color(0xFF4285F4)
                    )
                } else {
                    when (AssetUtils.getFileType(assetItem.name)) {
                        AssetFileType.MEI -> {
                            Icon(
                                painter = painterResource(R.drawable.mei_logo_simple_light),//Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .weight(1f)
                                    .align(Alignment.CenterHorizontally),
//                            tint = Color(0xFF4285F4)
                            )
                        }

                        AssetFileType.OTHER -> {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .weight(1f)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
//            }

//            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assetItem.name,
                textAlign = TextAlign.Left,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.primary
            )

//            if(assetItem.size != null) {
//                Text(
//                    text = if(assetItem.size>1024) "${assetItem.size/1024} KiB" else "${assetItem.size} B",
//                    textAlign = TextAlign.Left,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.secondary,
//                    fontStyle = FontStyle.Italic
//                )
//            }
        }
    }
}
