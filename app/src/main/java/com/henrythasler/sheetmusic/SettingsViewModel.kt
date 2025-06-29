package com.henrythasler.sheetmusic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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

class SettingsRepository(private val context: Context) {
    companion object {
        val SVG_OVERRIDE_FONT = stringPreferencesKey("Edwin-Roman")
        val SVG_RENDER_RESOLUTION = stringPreferencesKey("HIGH")
    }

    val svgOverrideFont: Flow<String?> = context.dataStore.data.map { preferences ->
        if(preferences[SVG_OVERRIDE_FONT] != "none") preferences[SVG_OVERRIDE_FONT] else null
    }
    suspend fun setSvgOverrideFont(name: String) {
        context.dataStore.edit { preferences ->
            preferences[SVG_OVERRIDE_FONT] = name
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
}


class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val svgOverrideFont = repository.svgOverrideFont
    val svgRenderResolution = repository.svgRenderResolution

    fun updateSvgOverrideFont(name: String) {
        viewModelScope.launch {
            repository.setSvgOverrideFont(name)
        }
    }

    fun updateSvgRenderResolution(value: SvgRenderResolutionEnum) {
        viewModelScope.launch {
            repository.setSvgRenderResolution(value)
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