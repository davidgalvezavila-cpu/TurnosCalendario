package com.example.turnoscalendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.turnoscalendario.data.Turno
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ListScreen(
    turnos: List<Turno>,
    onTurnoClick: (Turno) -> Unit
) {
    if (turnos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 48.dp), contentAlignment = Alignment.TopCenter) {
            Text(
                "Todavía no hay turnos registrados.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val turnosOrdenados = turnos.sortedWith(compareBy({ it.fecha }, { it.horaInicio ?: "" }))
    val agrupadosPorMes = turnosOrdenados.groupBy { it.fecha.substring(0, 7) } // yyyy-MM

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        agrupadosPorMes.forEach { (claveMes, turnosDelMes) ->
            item {
                val mes = YearMonth.parse(claveMes)
                val nombreMes = mes.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                Text(
                    text = "${nombreMes.replaceFirstChar { it.uppercase() }} ${mes.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            items(turnosDelMes) { turno ->
                val fecha = LocalDate.parse(turno.fecha)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTurnoClick(turno) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.width(48.dp)) {
                        Text(fecha.dayOfMonth.toString(), fontWeight = FontWeight.Bold)
                        Text(
                            fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "ES")),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colorDeHex(turno.colorHex))
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(turno.tipo)
                        if (turno.horaInicio != null) {
                            Text(
                                "${turno.horaInicio} - ${turno.horaFin ?: "?"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Divider()
            }
        }
    }
}
