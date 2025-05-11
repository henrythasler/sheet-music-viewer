package com.henrythasler.sheetmusic

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val isDirectory: Boolean
)

// File type enum for icon selection
enum class AssetFileType {
    FOLDER,
    MEI,
    OTHER
}

// Utility class for asset operations
object AssetUtils {
    fun getFileType(fileName: String): AssetFileType {
        // Check if it's a directory - this requires special handling in assets
        if (fileName.isEmpty()) return AssetFileType.FOLDER

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
                val fileList = mutableListOf<AssetItem>()

                // Get all files in the specified path
                val files = assetManager.list(path) ?: emptyArray()

                for (file in files) {
                    val fullPath = if (path.isEmpty()) file else "$path/$file"

                    // Check if it's a directory by seeing if it contains files
                    val isDirectory = assetManager.list(fullPath)?.isNotEmpty() ?: false

                    fileList.add(
                        AssetItem(
                            name = file,
                            path = fullPath,
                            isDirectory = isDirectory
                        )
                    )
                }

                // Sort alphabetically
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
    navController: NavHostController,
    viewModel: VerovioViewModel,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val meiAssetsFolder by viewModel.meiAssetsFolder.collectAsState()

    // Load assets when composable is first launched
    LaunchedEffect(meiAssetsFolder) {
        viewModel.readAssets(context)
    }

    Box(
        modifier = Modifier
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
                val assets = (uiState as AssetUiState.Success).assets
                LazyVerticalGrid (
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(assets) { assetItem ->
                        AssetGridItem(
                            assetItem = assetItem,
                            onClick = {
                                // Handle asset click
                                val message = "Selected: ${assetItem.name} (${assetItem.path})"
                                Log.d("AssetsGridScreen", message)

                                if(assetItem.isDirectory) {
                                    coroutineScope.launch {
                                        viewModel.readAssets(context, assetItem.path)
                                    }
                                }
                                else {
                                    navController.navigate(
                                        Screen.Notation.createRoute(assetItem.path, assetItem.name)
                                    )
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun AssetGridItem(
    assetItem: AssetItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card (
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                when (AssetUtils.getFileType(assetItem.name)) {
                    AssetFileType.FOLDER -> {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center),
                            tint = Color(0xFF4285F4)
                        )
                    }
                    AssetFileType.MEI -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center),
                            tint = Color(0xFF4285F4)
                        )
                    }
                    AssetFileType.OTHER -> {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assetItem.name,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
