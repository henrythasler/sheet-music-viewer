package com.henrythasler.sheetmusic

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VerovioViewModel : ViewModel() {
    private var path: String? = null

//    var _svgData by mutableStateOf<String?>(null)
//    var svgData by mutableStateOf<String?>(null)
//        private set
//    val svgData: State<String?>
//        get() = _svgData
    private val _svgData = MutableStateFlow("")
    val svgData = _svgData.asStateFlow()

    private val _verovioVersion = mutableStateOf<String?>(null)
    val verovioVersion: State<String?>
        get() = _verovioVersion

    fun getVerovioVersion() {
        viewModelScope.launch {
            _verovioVersion.value = getVersion()
        }
    }

    fun renderData() {
        viewModelScope.launch {
            if (!path.isNullOrEmpty()) {
                Log.i("VerovioViewModel", "using path '$path'")
                _svgData.value = renderData(path.toString())
//                Log.i("VerovioViewModel", "SVG: '${_svgData.value}'")
            } else {
                Log.e("VerovioViewModel", "Path is invalid")
            }
        }
    }

    // Function to extract and load asset
    fun loadAsset(context: Context, assetName: String) {
        viewModelScope.launch {
            path = extractAssetsFromDirectory(context, assetName)
        }
    }

    // Extract all assets from a directory to internal storage
    private fun extractAssetsFromDirectory(context: Context, directory: String): String {
        val rootPath = context.filesDir.absolutePath +
                (if (directory.isNotEmpty()) "/$directory" else "")

        try {
            val assetList = context.assets.list(directory) ?: return rootPath

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
    private external fun renderData(path: String): String
}