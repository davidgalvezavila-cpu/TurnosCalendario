@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.turnoscalendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.turnoscalendario.data.TIPOS_TURNO_PREDEFINIDOS
import com.example.turnoscalendario.data.TipoTurno
import com.example.turnoscalendario.data.Turno
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Diálogo que muestra todos los turnos de un día (puede haber varios: turnos partidos). */
@Composable
fun DialogoDia(
    fecha: LocalDate,
    turnos: List<Turno>,
    onNuevoTurno: () -> Unit,
    onEditarTurno: (Turno) -> Unit,
    onEliminarTurno: (Turno) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            Text("${nombreDia.replaceFirstChar { it.uppercase() }} ${fecha.dayOfMonth}/${fecha.monthValue}")
        },
        text = {
            Column {
                if (turnos.isEmpty()) {
                    Text(
                        "No hay turnos este día.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    turnos.sortedBy { it.horaInicio ?: "" }.forEach { turno ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditarTurno(turno) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(colorDeHex(turno.colorHex))
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(turno.tipo, fontWeight = FontWeight.Medium)
                                if (turno.horaInicio != null) {
                                    Text(
                                        "${turno.horaInicio} - ${turno.horaFin ?: "?"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            if (turno.recordatorioMinutosAntes != null) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Tiene recordatorio",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            IconButton(onClick = { onEliminarTurno(turno) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar turno")
                            }
                        }
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onNuevoTurno) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir turno")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cerrar") }
        }
    )
}

/** Diálogo para crear o editar un turno concreto (con horario y recordatorio propios). */
@Composable
fun DialogoEditarTurno(
    fecha: LocalDate,
    turnoExistente: Turno?,
    minutosRecordatorioPorDefecto: Int,
    onGuardar: (Turno) -> Unit,
    onCerrar: () -> Unit
) {
    var tipoSeleccionado by remember {
        mutableStateOf(
            TIPOS_TURNO_PREDEFINIDOS.find { it.nombre == turnoExistente?.tipo } ?: TIPOS_TURNO_PREDEFINIDOS.first()
        )
    }
    var horaInicio by remember {
        mutableStateOf(turnoExistente?.horaInicio ?: tipoSeleccionado.horaInicio.takeIf { it != "-" } ?: "")
    }
    var horaFin by remember {
        mutableStateOf(turnoExistente?.horaFin ?: tipoSeleccionado.horaFin.takeIf { it != "-" } ?: "")
    }
    var notas by remember { mutableStateOf(turnoExistente?.notas ?: "") }
    var recordatorioActivado by remember { mutableStateOf(turnoExistente?.recordatorioMinutosAntes != null) }
    var minutosRecordatorio by remember {
        mutableStateOf(turnoExistente?.recordatorioMinutosAntes ?: minutosRecordatorioPorDefecto)
    }

    fun seleccionarTipo(tipo: TipoTurno) {
        tipoSeleccionado = tipo
        horaInicio = tipo.horaInicio.takeIf { it != "-" } ?: ""
        horaFin = tipo.horaFin.takeIf { it != "-" } ?: ""
    }

    val entradaValida = (horaInicio.isBlank() || esHoraValida(horaInicio)) &&
        (horaFin.isBlank() || esHoraValida(horaFin))

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (turnoExistente != null) "Editar turno" else "Nuevo turno") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Tipo de turno", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TIPOS_TURNO_PREDEFINIDOS.forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { seleccionarTipo(tipo) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = tipoSeleccionado.nombre == tipo.nombre,
                            onClick = { seleccionarTipo(tipo) }
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colorDeHex(tipo.colorHex))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(tipo.nombre)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Inicio (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = horaInicio.isNotBlank() && !esHoraValida(horaInicio)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        label = { Text("Fin (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = horaFin.isNotBlank() && !esHoraValida(horaFin)
                    )
                }
                Text(
                    "Formato 24h, ej. 08:00. Dos turnos el mismo día con horas distintas = turno partido.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Recordarme antes de empezar")
                    }
                    Switch(checked = recordatorioActivado, onCheckedChange = { recordatorioActivado = it })
                }
                if (recordatorioActivado) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 60, 120).forEach { minutos ->
                            FilterChip(
                                selected = minutosRecordatorio == minutos,
                                onClick = { minutosRecordatorio = minutos },
                                label = { Text(if (minutos < 60) "${minutos}min" else "${minutos / 60}h") }
                            )
                        }
                    }
                    if (!esHoraValida(horaInicio)) {
                        Text(
                            "Indica una hora de inicio válida para poder programar el recordatorio.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGuardar(
                        Turno(
                            id = turnoExistente?.id ?: 0,
                            fecha = fecha.format(FORMATO_FECHA),
                            tipo = tipoSeleccionado.nombre,
                            colorHex = tipoSeleccionado.colorHex,
                            horaInicio = horaInicio.takeIf { it.isNotBlank() },
                            horaFin = horaFin.takeIf { it.isNotBlank() },
                            notas = notas.ifBlank { null },
                            recordatorioMinutosAntes = if (recordatorioActivado && esHoraValida(horaInicio)) minutosRecordatorio else null,
                            eventoCalendarioId = turnoExistente?.eventoCalendarioId
                        )
                    )
                },
                enabled = entradaValida
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}
