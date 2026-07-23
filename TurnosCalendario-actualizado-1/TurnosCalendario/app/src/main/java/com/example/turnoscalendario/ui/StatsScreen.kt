package com.example.turnoscalendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StatsScreen(viewModel: CalendarViewModel) {
    val mesEstadisticas by viewModel.mesEstadisticas.collectAsState()
    val estadisticas by viewModel.estadisticasDelMes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::mesEstadisticasAnterior) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
            }
            val nombreMes = mesEstadisticas.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            Text(
                "${nombreMes.replaceFirstChar { it.uppercase() }} ${mesEstadisticas.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = viewModel::mesEstadisticasSiguiente) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
            }
        }

        Spacer(Modifier.height(16.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Horas totales trabajadas", style = MaterialTheme.typography.labelLarge)
                Text(
                    formatoHoras(estadisticas.horasTotales),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${estadisticas.diasConTurno} días con turno asignado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Horas por tipo de turno", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        if (estadisticas.porTipo.isEmpty()) {
            Text(
                "No hay turnos registrados en este mes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val maxHoras = estadisticas.porTipo.maxOf { it.horas }.coerceAtLeast(1.0)
            LazyColumn {
                items(estadisticas.porTipo) { item ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.tipo, fontWeight = FontWeight.Medium)
                            Text(
                                "${formatoHoras(item.horas)} · ${item.turnos} turno${if (item.turnos == 1) "" else "s"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (item.horas / maxHoras).toFloat().coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorDeHex(item.colorHex))
                            )
                        }
                    }
                }
            }
        }
    }
}
