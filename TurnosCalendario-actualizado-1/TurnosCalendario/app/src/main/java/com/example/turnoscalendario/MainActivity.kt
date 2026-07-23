package com.example.turnoscalendario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.turnoscalendario.ui.CalendarViewModel
import com.example.turnoscalendario.ui.TurnosApp
import com.example.turnoscalendario.ui.theme.TurnosCalendarioTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TurnosCalendarioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TurnosApp(viewModel = viewModel)
                }
            }
        }
    }
}
