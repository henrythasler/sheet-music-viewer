package com.henrythasler.sheetmusic

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val SVG_OVERRIDE_FONT = stringPreferencesKey("Edwin-Roman")
        val CANVAS_BITMAP_RESOLUTION = stringPreferencesKey("username")
    }

    val svgOverrideFont: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SVG_OVERRIDE_FONT] ?: ""
    }

    suspend fun setSvgOverrideFont(name: String) {
        context.dataStore.edit { preferences ->
            preferences[SVG_OVERRIDE_FONT] = name
        }
    }
}


class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val svgOverrideFont = repository.svgOverrideFont

    fun updateSvgOverrideFont(name: String) {
        viewModelScope.launch {
            repository.setSvgOverrideFont(name)
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