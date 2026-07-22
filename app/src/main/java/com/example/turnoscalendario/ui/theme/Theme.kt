package com.example.turnoscalendario.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresApp = lightColorScheme(
    primary = Color(0xFF3F51B5),
    secondary = Color(0xFF03A9F4)
)

@Composable
fun TurnosCalendarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColoresApp,
        content = content
    )
}
