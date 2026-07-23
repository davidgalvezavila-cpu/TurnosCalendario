@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.turnoscalendario.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.turnoscalendario.calendarsync.CalendarioInfo
import com.example.turnoscalendario.calendarsync.GoogleCalendarSync

@Composable
fun SettingsScreen(viewModel: CalendarViewModel) {
    val context = LocalContext.current
    val preferencias = viewModel.preferencias

    var sincronizacionActivada by remember { mutableStateOf(preferencias.sincronizacionActivada) }
    var calendarios by remember { mutableStateOf<List<CalendarioInfo>>(emptyList()) }
    var calendarioSeleccionadoId by remember { mutableStateOf(preferencias.calendarioIdSeleccionado) }
    var expandidoDropdown by remember { mutableStateOf(false) }
    var recordatorioDefecto by remember { mutableStateOf(preferencias.recordatorioMinutosPorDefecto) }
    var sincronizando by remember { mutableStateOf(false) }

    fun tienePermisosCalendario(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED

    val lanzadorPermisosCalendario = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        val concedido = resultados.values.all { it }
        sincronizacionActivada = concedido
        preferencias.sincronizacionActivada = concedido
        if (concedido) calendarios = GoogleCalendarSync.listarCalendarios(context)
    }

    LaunchedEffect(Unit) {
        if (tienePermisosCalendario()) {
            calendarios = GoogleCalendarSync.listarCalendarios(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Recordatorios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Minutos de aviso por defecto al crear un turno nuevo:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30, 60, 120).forEach { minutos ->
                FilterChip(
                    selected = recordatorioDefecto == minutos,
                    onClick = {
                        recordatorioDefecto = minutos
                        preferencias.recordatorioMinutosPorDefecto = minutos
                    },
                    label = { Text(if (minutos < 60) "$minutos min" else "${minutos / 60} h") }
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Divider()
        Spacer(Modifier.height(28.dp))

        Text("Sincronización con Google Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Los turnos se añaden como eventos en un calendario del dispositivo. Si eliges tu calendario de Google, quedarán sincronizados con tu cuenta automáticamente.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("Activar sincronización") }
            Switch(
                checked = sincronizacionActivada,
                onCheckedChange = { activar ->
                    if (activar && !tienePermisosCalendario()) {
                        lanzadorPermisosCalendario.launch(
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                        )
                    } else {
                        sincronizacionActivada = activar
                        preferencias.sincronizacionActivada = activar
                        if (activar) calendarios = GoogleCalendarSync.listarCalendarios(context)
                    }
                }
            )
        }

        if (sincronizacionActivada) {
            Spacer(Modifier.height(12.dp))
            if (calendarios.isEmpty()) {
                Text(
                    "No se encontraron calendarios en el dispositivo. Añade una cuenta de Google desde los ajustes de Android para poder elegirla aquí.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                val seleccionado = calendarios.find { it.id == calendarioSeleccionadoId }
                ExposedDropdownMenuBox(
                    expanded = expandidoDropdown,
                    onExpandedChange = { expandidoDropdown = it }
                ) {
                    OutlinedTextField(
                        value = seleccionado?.let { "${it.nombre} (${it.cuenta})" } ?: "Elegir calendario",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Calendario destino") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoDropdown,
                        onDismissRequest = { expandidoDropdown = false }
                    ) {
                        calendarios.forEach { calendario ->
                            DropdownMenuItem(
                                text = { Text("${calendario.nombre} (${calendario.cuenta})") },
                                onClick = {
                                    calendarioSeleccionadoId = calendario.id
                                    preferencias.calendarioIdSeleccionado = calendario.id
                                    expandidoDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        sincronizando = true
                        viewModel.sincronizarTodoConCalendario { sincronizando = false }
                    },
                    enabled = calendarioSeleccionadoId > 0 && !sincronizando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (sincronizando) "Sincronizando..." else "Sincronizar todos los turnos ahora")
                }
            }
        }
    }
}
