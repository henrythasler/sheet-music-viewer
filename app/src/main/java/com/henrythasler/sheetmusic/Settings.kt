package com.henrythasler.sheetmusic

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: VerovioViewModel,
) {
    val context = LocalContext.current
    var checked by remember { mutableStateOf(true) }

    Column {
        Text(
            "Minimal checkbox $checked"
        )
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}