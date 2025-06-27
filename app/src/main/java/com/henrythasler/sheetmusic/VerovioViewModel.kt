package com.henrythasler.sheetmusic

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VerovioViewModel : ViewModel() {
//    private val _svgData = MutableStateFlow("")
//    val svgData = _svgData.asStateFlow()

    private val _verovioVersion = mutableStateOf<String?>(null)
    val verovioVersion: State<String?>
        get() = _verovioVersion

    private val _uiState = MutableStateFlow<AssetUiState>(AssetUiState.Loading)
    val uiState: StateFlow<AssetUiState> = _uiState.asStateFlow()

    private val _meiAssetsFolder = MutableStateFlow("mei")
    var meiAssetsFolder = _meiAssetsFolder.asStateFlow()

    suspend fun readAssets(context: Context, newFolder: String? = null) {
        _uiState.value = AssetUiState.Loading

        if(newFolder != null) {
            _meiAssetsFolder.value = newFolder
        }

        try {
            val assets = AssetUtils.listAssetsInPath(context, _meiAssetsFolder.value).toMutableList()
            _uiState.value = if (assets.isEmpty()) {
                AssetUiState.Empty
            } else {
                AssetUiState.Success(assets)
            }
        } catch (e: Exception) {
            _uiState.value = AssetUiState.Error("Error loading assets: ${e.message}")
        }
    }

    fun getVerovioVersion() {
        viewModelScope.launch {
            _verovioVersion.value = getVersion()
        }
    }

//    fun renderAsset(context: Context, assetPath: String) {
//        viewModelScope.launch {
//            val timeMillis = measureTimeMillis {
//                try {
//                    val encodedMusic = context.assets.open(assetPath).bufferedReader().use { it.readText() }
//                    _svgData.value = renderData(encodedMusic)
//                } catch (e: Exception) {
//                    "Failed to load asset: ${e.localizedMessage}"
//                }
//            }
//
//            Log.i("Verovio", "Engraving '$assetPath' took $timeMillis ms. (${_svgData.value.length} Bytes)")
//        }
//    }

    suspend fun engraveMusicAsset(context: Context, assetPath: String): String? = withContext(
        Dispatchers.IO) {
        try {
            val encodedMusic = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            val svgData = renderData(encodedMusic)
            if(svgData.isNotEmpty()) {
                return@withContext svgData
            }
        } catch (e: Exception) {
            "Failed to load asset: ${e.localizedMessage}"
        }
        return@withContext null
    }

    // Function to extract and load asset
    fun extractAssets(context: Context) {
        viewModelScope.launch {
            val verovioDataPath = extractAssetsFromDirectory(context, "data")
            Log.i("VerovioViewModel", "using path '$verovioDataPath'")
            setDataPath(verovioDataPath)
        }
    }

    // Extract all assets from a directory to internal storage
    private fun extractAssetsFromDirectory(context: Context, directory: String): String {
        val rootPath = context.filesDir.absolutePath +
                (if (directory.isNotEmpty()) "/$directory" else "")

        try {
            val assetList = context.assets.list(directory) ?: return rootPath

            Log.i("VerovioViewModel", "Extracting '$directory'")

            // Create the root directory if needed
            val rootDir = File(rootPath)
            if (!rootDir.exists()) {
                rootDir.mkdirs()
            }

            for (asset in assetList) {
                val assetPath = if (directory.isEmpty()) asset else "$directory/$asset"
                try {
                    // Check if it's a file or directory
                    val subAssets = context.assets.list(assetPath)
                    if (subAssets.isNullOrEmpty()) {
                        // It's a file, extract it
                        extractAssetForNative(context, assetPath)
                    } else {
                        // It's a directory, recurse
                        extractAssetsFromDirectory(context, assetPath)
                    }
                } catch (e: IOException) {
                    Log.e("AssetExtractor", "Failed to extract: $assetPath", e)
                }
            }
        } catch (e: IOException) {
            Log.e("AssetExtractor", "Failed to list assets in: $directory", e)
        }

        // Return just the root path where files were extracted
        return rootPath
    }

    // Extract asset to app's internal storage
    private fun extractAssetForNative(context: Context, assetName: String): String {
        val outputFile = File(context.filesDir, assetName)

        try {
            outputFile.parentFile?.mkdirs()

            if (!outputFile.exists()) {
                context.assets.open(assetName).use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            return outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("AssetExtractor", "Failed to extract asset: $assetName", e)
            return ""
        }
    }

    init {
        System.loadLibrary("sheetmusic")
    }
    private external fun getVersion(): String
    private external fun renderData(data: String): String
    private external fun renderToTimemap(data: String): String
    private external fun setDataPath(path: String)
}