package com.henrythasler.sheetmusic

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SvgRenderResolutionEnum { LOW, MEDIUM, HIGH;
    companion object {
        fun fromString(value: String): SvgRenderResolutionEnum {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                HIGH // default fallback
            }
        }
    }
}

// Asset item class representing a file in the assets folder
data class CustomFont(
    val description: String,    // shown in the UI
    val familyName: String? = null, // used in SVG
    val path: String? = null,   // used to load from assets
    var fontFamily: FontFamily? = null, // used in the font resolver
)

// Define your custom font families
val SvgCustomFonts = mapOf(
    "none" to CustomFont("off (use system font)"),
    "Edwin-Roman" to CustomFont("Edwin Roman", "Edwin-Roman", "fonts/Edwin-Roman.otf"),
    "Edwin-Italic" to CustomFont("Edwin Italic", "Edwin-Italic", "fonts/Edwin-Italic.otf"),
    "EBGaramond" to CustomFont("EB Garamond", "EBGaramond", "fonts/EBGaramond.ttf"),
    "EBGaramond-Italic" to CustomFont("EB Garamond Italic", "EBGaramond-Italic", "fonts/EBGaramond-Italic.ttf"),
    "OpenSans" to CustomFont("Open Sans", "OpenSans", "fonts/OpenSans.ttf"),
    "OpenSans-Italic" to CustomFont("Open Sans Italic", "OpenSans-Italic", "fonts/OpenSans-Italic.ttf"),
    "OpenSans-Light" to CustomFont("Open Sans Light", "OpenSans-Light", "fonts/OpenSans-Light.ttf"),
    "OpenSans-CondLight" to CustomFont("Open Sans Condensed Light", "OpenSans-CondLight", "fonts/OpenSans-CondLight.ttf"),
)

val SvgRenderResolutionMapping = mapOf(
    SvgRenderResolutionEnum.HIGH to 1f,
    SvgRenderResolutionEnum.MEDIUM to 0.70710677f,    // 1/sqrt(2)
    SvgRenderResolutionEnum.LOW to 0.5f,
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val SVG_FONT_OVERRIDE = stringPreferencesKey("SVG_FONT_OVERRIDE")
        val SVG_RENDER_RESOLUTION = stringPreferencesKey("SVG_RENDER_RESOLUTION")
        val VRV_LYRIC_SIZE_SCALE = floatPreferencesKey("VRV_LYRIC_SIZE_SCALE")
        val VRV_LYRIC_WORD_SPACE_SCALE = floatPreferencesKey("VRV_LYRIC_WORD_SPACE_SCALE")
        val SHOW_DEBUG_INFO = booleanPreferencesKey("SHOW_DEBUG_INFO")
    }

    val svgFontOverride: Flow<String?> = context.dataStore.data.map { preferences ->
        if(preferences[SVG_FONT_OVERRIDE] != "none") preferences[SVG_FONT_OVERRIDE] else null
    }
    suspend fun setSvgFontOverride(name: String) {
        context.dataStore.edit { preferences ->
            preferences[SVG_FONT_OVERRIDE] = name
        }
    }

    val vrvLyricSizeScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[VRV_LYRIC_SIZE_SCALE] ?: 0.8f
    }
    suspend fun setVrvLyricSizeScale(value: Float) {
        context.dataStore.edit { preferences ->
            Log.d("SettingsRepository", "$value")
            preferences[VRV_LYRIC_SIZE_SCALE] = value
        }
    }

    val vrvLyricWordSpaceScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[VRV_LYRIC_WORD_SPACE_SCALE] ?: 4.0f
    }
    suspend fun setVrvLyricWordSpaceScale(value: Float) {
        context.dataStore.edit { preferences ->
            Log.d("SettingsRepository", "$value")
            preferences[VRV_LYRIC_WORD_SPACE_SCALE] = value
        }
    }

    val svgRenderResolution: Flow<SvgRenderResolutionEnum> = context.dataStore.data.map { preferences ->
        SvgRenderResolutionEnum.fromString(preferences[SVG_RENDER_RESOLUTION] ?: SvgRenderResolutionEnum.HIGH.name)
    }
    suspend fun setSvgRenderResolution(value: SvgRenderResolutionEnum) {
        context.dataStore.edit { preferences ->
            preferences[SVG_RENDER_RESOLUTION] = value.name
        }
    }

    val showDebugInfo: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_DEBUG_INFO] ?: false
    }
    suspend fun setShowDebugInfo(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_DEBUG_INFO] = enabled
        }
    }
}


class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val svgFontOverride = repository.svgFontOverride
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val svgRenderResolution = repository.svgRenderResolution
        .stateIn(viewModelScope, SharingStarted.Eagerly, SvgRenderResolutionEnum.HIGH)
    val vrvLyricSizeScale = repository.vrvLyricSizeScale
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.8f)
    val vrvLyricWordSpaceScale = repository.vrvLyricWordSpaceScale
        .stateIn(viewModelScope, SharingStarted.Eagerly, 4.0f)
    val showDebugInfo = repository.showDebugInfo
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // temporary settings and global objects
    var currentOffset: Offset = Offset.Zero
    var currentScale: Float = 1.0f
    var verovioVersion: String = ""

    fun updateSvgFontOverride(name: String) {
        viewModelScope.launch {
            repository.setSvgFontOverride(name)
        }
    }

    fun updateVrvLyricSizeScale(value: Float) {
        viewModelScope.launch {
            repository.setVrvLyricSizeScale(value)
        }
    }

    fun updateVrvLyricWordSpaceScale(value: Float) {
        viewModelScope.launch {
            repository.setVrvLyricWordSpaceScale(value)
        }
    }

    fun updateSvgRenderResolution(value: SvgRenderResolutionEnum) {
        viewModelScope.launch {
            repository.setSvgRenderResolution(value)
        }
    }

    fun updateShowDebugInfo(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShowDebugInfo(enabled)
        }
    }
}

val LocalSettingsViewModel = compositionLocalOf<SettingsViewModel> {
    error("SettingsViewModel not provided")
}

@Composable
fun useSettings(): SettingsViewModel {
    return LocalSettingsViewModel.current
}

class SettingsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = SettingsRepository(context)
        return SettingsViewModel(repo) as T
    }
}