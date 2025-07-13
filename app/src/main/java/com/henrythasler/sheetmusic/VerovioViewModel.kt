package com.henrythasler.sheetmusic

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Float.max
import java.lang.Float.min
import java.util.Locale

class VerovioViewModel : ViewModel() {
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

    fun getVerovioVersion(): String {
        return tkGetVersion()
    }

    fun getPageCount(): Int {
        return tkGetPageCount()
    }

    suspend fun loadData(data: String): Boolean = withContext(
        Dispatchers.IO) {
        try {
            val res = tkLoadData(data)
            return@withContext res
        } catch (e: Exception) {
            "Failed to load asset: ${e.localizedMessage}"
        }
        return@withContext false
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

    suspend fun loadMusicAsset(context: Context, assetPath: String, fontScale: Float): Boolean = withContext(
        Dispatchers.IO) {
        try {
            val data = context.assets.open(assetPath).bufferedReader().use { it.readText() }
            var tkOptions = emptyArray<String>()

            tkOptions += "\"smuflTextFont\": \"none\""
            tkOptions += "\"svgFormatRaw\": true"

            tkOptions += "\"header\": \"auto\""
            tkOptions += "\"footer\": \"none\""

            tkOptions += "\"breaks\": \"auto\""
            tkOptions += "\"adjustPageWidth\": true"
            tkOptions += "\"adjustPageHeight\": true"

            tkOptions += "\"pageMarginLeft\": 50"
            tkOptions += "\"pageMarginRight\": 50"
            tkOptions += "\"pageMarginTop\": 10"
            tkOptions += "\"pageMarginBottom\": 10"

            tkOptions += "\"lyricSize\": %f".format(Locale.US, min(8.0f, max(2.0f, 4.5f * fontScale)))
            tkOptions += "\"lyricWordSpace\": 4.0"
            tkOptions += "\"multiRestStyle\": \"block\""

            if(!tkSetOptions(tkOptions))
            {
                return@withContext false
            }

            if(!tkLoadData(data)) {
                return@withContext false
            }

            return@withContext true
        } catch (e: Exception) {
            "Failed to load asset: ${e.localizedMessage}"
        }
        return@withContext false
    }

    suspend fun engravePage(pageNo: Int, xmlDeclaration: Boolean = false): String? = withContext(
        Dispatchers.IO) {
        try {
            val svgData = tkRenderToSVG(pageNo, xmlDeclaration)
            if(svgData.isNotEmpty()) {
                return@withContext svgData
            }
        } catch (e: Exception) {
            "Failed to load asset: ${e.localizedMessage}"
        }
        return@withContext null
    }

    suspend fun getTimemap(): String? = withContext(
        Dispatchers.IO) {
        try {
            var tkOptions = emptyArray<String>()

            tkOptions += "\"includeMeasures\": false"
            tkOptions += "\"includeRests\": false"

            val timemap = tkRenderToTimemap(tkOptions)
//            val svgData = tkRenderData(encodedMusic, fontScale)
            if(timemap.isNotEmpty()) {
                return@withContext timemap
            }
        } catch (e: Exception) {
            "Failed to generate Timemap: ${e.localizedMessage}"
        }
        return@withContext null
    }

    // Function to extract and load asset
    fun extractAssets(context: Context) {
        viewModelScope.launch {
            val commitHash = BuildConfig.COMMIT_HASH
            val verovioDataPath = "${context.filesDir.absolutePath}/data"

            fun checkFileExists(path: String): Boolean {
                return try {
                    FileInputStream(path).use { true }
                } catch (e: IOException) {
                    false
                }
            }

            val checkPointFile = "$verovioDataPath/$commitHash"
            if(!checkFileExists(checkPointFile)) {
                extractAssetsFromDirectory(context, "data", verovioDataPath)
                FileOutputStream(checkPointFile).use { }
            }

            Log.i("VerovioViewModel", "using path '$verovioDataPath'")
            tkSetResourcePath(verovioDataPath)
        }
    }

    // Extract all assets from a directory to internal storage
    private fun extractAssetsFromDirectory(context: Context, directory: String, rootPath: String): String {
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
                        extractAssetsFromDirectory(context, assetPath, rootPath)
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
    private external fun tkGetVersion(): String
    private external fun tkGetPageCount(): Int
    private external fun tkSetResourcePath(path: String): Boolean
    private external fun tkSetOptions(options: Array<String>): Boolean
    private external fun tkLoadData(data: String): Boolean
    private external fun tkRenderToSVG(pageNo: Int, xmlDeclaration: Boolean): String
    private external fun tkRenderToTimemap(options: Array<String>): String

    private external fun tkRenderData(data: String, fontScale: Float): String
}