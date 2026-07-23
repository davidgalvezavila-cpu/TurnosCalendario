@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.turnoscalendario.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.turnoscalendario.data.Turno
import com.example.turnoscalendario.notifications.RecordatorioReceiver
import java.time.LocalDate

private enum class Pantalla { CALENDARIO, ESTADISTICAS, AJUSTES }

private data class EdicionTurno(val fecha: LocalDate, val turno: Turno?)

@Composable
fun TurnosApp(viewModel: CalendarViewModel) {
    val context = LocalContext.current
    var pantalla by remember { mutableStateOf(Pantalla.CALENDARIO) }
    var diaSeleccionado by remember { mutableStateOf<LocalDate?>(null) }
    var edicionTurno by remember { mutableStateOf<EdicionTurno?>(null) }
    var diasSeleccionMultiple by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var mostrarDialogoAplicarMultiple by remember { mutableStateOf(false) }

    val todosLosTurnos by viewModel.todosLosTurnos.collectAsState()

    val lanzadorNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* si se deniega, simplemente no se mostrarán notificaciones */ }

    LaunchedEffect(Unit) {
        RecordatorioReceiver.crearCanalNotificacion(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!concedido) lanzadorNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        bottomBar = {
            if (diasSeleccionMultiple.isNotEmpty()) {
                BottomAppBar {
                    TextButton(onClick = { diasSeleccionMultiple = emptySet() }) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${diasSeleccionMultiple.size} día(s) seleccionado(s)",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { mostrarDialogoAplicarMultiple = true }) {
                        Text("Aplicar turno")
                    }
                }
            } else {
            NavigationBar {
                NavigationBarItem(
                    selected = pantalla == Pantalla.CALENDARIO,
                    onClick = { pantalla = Pantalla.CALENDARIO },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Calendario") }
                )
                NavigationBarItem(
                    selected = pantalla == Pantalla.ESTADISTICAS,
                    onClick = { pantalla = Pantalla.ESTADISTICAS },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Estadísticas") }
                )
                NavigationBarItem(
                    selected = pantalla == Pantalla.AJUSTES,
                    onClick = { pantalla = Pantalla.AJUSTES },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Ajustes") }
                )
            }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (pantalla) {
                Pantalla.CALENDARIO -> CalendarScreen(
                    viewModel = viewModel,
                    onDiaClick = { fecha -> diaSeleccionado = fecha },
                    diasSeleccionados = diasSeleccionMultiple,
                    onLargoPresionarDia = { fecha ->
                        diasSeleccionMultiple = diasSeleccionMultiple + fecha
                    },
                    onTocarDiaEnSeleccion = { fecha ->
                        diasSeleccionMultiple =
                            if (fecha in diasSeleccionMultiple) diasSeleccionMultiple - fecha
                            else diasSeleccionMultiple + fecha
                    }
                )
                Pantalla.ESTADISTICAS -> StatsScreen(viewModel)
                Pantalla.AJUSTES -> SettingsScreen(viewModel)
            }
        }
    }

    // Detalle del día: lista de turnos de esa fecha (puede haber varios = turno partido).
    diaSeleccionado?.let { fecha ->
        val turnosDia = todosLosTurnos.filter { it.fecha == fecha.format(FORMATO_FECHA) }
        DialogoDia(
            fecha = fecha,
            turnos = turnosDia,
            onNuevoTurno = {
                edicionTurno = EdicionTurno(fecha, null)
                diaSeleccionado = null
            },
            onEditarTurno = { turno ->
                edicionTurno = EdicionTurno(fecha, turno)
                diaSeleccionado = null
            },
            onEliminarTurno = { turno -> viewModel.eliminarTurno(turno) },
            onCerrar = { diaSeleccionado = null }
        )
    }

    if (mostrarDialogoAplicarMultiple) {
        DialogoAplicarTurnoMultiple(
            cantidadDias = diasSeleccionMultiple.size,
            onAplicar = { tipo ->
                viewModel.aplicarTipoTurnoAFechas(diasSeleccionMultiple.toList(), tipo)
                mostrarDialogoAplicarMultiple = false
                diasSeleccionMultiple = emptySet()
            },
            onCerrar = { mostrarDialogoAplicarMultiple = false }
        )
    }

    // Edición/creación de un turno concreto; al terminar, volvemos al detalle del día.
    edicionTurno?.let { edicion ->
        DialogoEditarTurno(
            fecha = edicion.fecha,
            turnoExistente = edicion.turno,
            minutosRecordatorioPorDefecto = viewModel.preferencias.recordatorioMinutosPorDefecto,
            onGuardar = { turno ->
                viewModel.guardarTurno(turno)
                edicionTurno = null
                diaSeleccionado = edicion.fecha
            },
            onCerrar = {
                edicionTurno = null
                diaSeleccionado = edicion.fecha
            }
        )
    }
}
