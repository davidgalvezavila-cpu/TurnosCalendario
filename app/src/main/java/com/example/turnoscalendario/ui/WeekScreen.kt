package com.example.turnoscalendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.turnoscalendario.data.Turno
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekScreen(
    fechaReferencia: LocalDate,
    turnos: List<Turno>,
    onDiaClick: (LocalDate) -> Unit
) {
    val inicioSemana = fechaReferencia.with(DayOfWeek.MONDAY)
    val dias = (0..6).map { inicioSemana.plusDays(it.toLong()) }
    val turnosPorFecha = turnos.groupBy { it.fecha }
    val hoy = LocalDate.now()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(dias) { dia ->
            val turnosDia = turnosPorFecha[dia.format(FORMATO_FECHA)] ?: emptyList()
            val esHoy = dia == hoy

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onDiaClick(dia) },
                colors = CardDefaults.cardColors(
                    containerColor = if (esHoy) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(56.dp)) {
                        Text(
                            dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
                                .replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.Bold
                        )
                        Text(dia.dayOfMonth.toString(), style = MaterialTheme.typography.bodySmall)
                    }

                    if (turnosDia.isEmpty()) {
                        Text(
                            "Sin turno",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column(modifier = Modifier.weight(1f)) {
                            turnosDia.forEach { turno ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(colorDeHex(turno.colorHex))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    val horario = if (turno.horaInicio != null)
                                        " · ${turno.horaInicio}-${turno.horaFin ?: "?"}" else ""
                                    Text("${turno.tipo}$horario", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
