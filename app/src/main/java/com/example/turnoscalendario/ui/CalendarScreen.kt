@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.turnoscalendario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.turnoscalendario.data.TIPOS_TURNO_PREDEFINIDOS
import com.example.turnoscalendario.data.Turno
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel, onDiaClick: (LocalDate) -> Unit) {
    val modoVista by viewModel.modoVista.collectAsState()
    val fechaReferencia by viewModel.fechaReferencia.collectAsState()
    val turnosVisibles by viewModel.turnosVisibles.collectAsState()
    val todosLosTurnos by viewModel.todosLosTurnos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        SelectorDeModo(modoActual = modoVista, onCambiarModo = viewModel::cambiarModoVista)

        if (modoVista != ModoVista.LISTA) {
            Spacer(Modifier.height(4.dp))
            SelectorDeFecha(
                modoVista = modoVista,
                fechaReferencia = fechaReferencia,
                onAnterior = viewModel::irAnterior,
                onSiguiente = viewModel::irSiguiente,
                onHoy = viewModel::irAHoy
            )
        }

        Spacer(Modifier.height(4.dp))

        when (modoVista) {
            ModoVista.MENSUAL -> {
                EncabezadoDiasSemana()
                Spacer(Modifier.height(4.dp))
                CuadriculaDelMes(
                    mes = YearMonth.from(fechaReferencia),
                    turnosPorFecha = turnosVisibles.groupBy { it.fecha },
                    onDiaClick = onDiaClick
                )
                Spacer(Modifier.height(16.dp))
                Leyenda()
                Spacer(Modifier.height(16.dp))
            }
            ModoVista.SEMANAL -> {
                WeekScreen(
                    fechaReferencia = fechaReferencia,
                    turnos = turnosVisibles,
                    onDiaClick = onDiaClick
                )
            }
            ModoVista.LISTA -> {
                ListScreen(
                    turnos = todosLosTurnos,
                    onTurnoClick = { turno -> onDiaClick(LocalDate.parse(turno.fecha)) }
                )
            }
        }
    }
}

@Composable
private fun SelectorDeModo(modoActual: ModoVista, onCambiarModo: (ModoVista) -> Unit) {
    val opciones = listOf(
        ModoVista.MENSUAL to "Mes",
        ModoVista.SEMANAL to "Semana",
        ModoVista.LISTA to "Lista"
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        opciones.forEachIndexed { index, (modo, etiqueta) ->
            SegmentedButton(
                selected = modoActual == modo,
                onClick = { onCambiarModo(modo) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = opciones.size)
            ) {
                Text(etiqueta)
            }
        }
    }
}

@Composable
private fun SelectorDeFecha(
    modoVista: ModoVista,
    fechaReferencia: LocalDate,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onHoy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onAnterior) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
        }

        val etiqueta = if (modoVista == ModoVista.SEMANAL) {
            val inicio = fechaReferencia.with(DayOfWeek.MONDAY)
            val fin = inicio.plusDays(6)
            val nombreMesFin = fin.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            "${inicio.dayOfMonth} - ${fin.dayOfMonth} ${nombreMesFin.replaceFirstChar { it.uppercase() }}"
        } else {
            val mes = YearMonth.from(fechaReferencia)
            val nombreMes = mes.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            "${nombreMes.replaceFirstChar { it.uppercase() }} ${mes.year}"
        }

        Text(
            text = etiqueta,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onHoy() }
        )

        IconButton(onClick = onSiguiente) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
        }
    }
}

@Composable
private fun EncabezadoDiasSemana() {
    val dias = listOf("L", "M", "X", "J", "V", "S", "D")
    Row(modifier = Modifier.fillMaxWidth()) {
        dias.forEach { dia ->
            Text(
                text = dia,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CuadriculaDelMes(
    mes: YearMonth,
    turnosPorFecha: Map<String, List<Turno>>,
    onDiaClick: (LocalDate) -> Unit
) {
    val primerDiaDelMes = mes.atDay(1)
    val espaciosVacios = primerDiaDelMes.dayOfWeek.value - DayOfWeek.MONDAY.value
    val diasDelMes = mes.lengthOfMonth()
    val hoy = LocalDate.now()

    val celdas: List<LocalDate?> = buildList {
        repeat(espaciosVacios) { add(null) }
        for (dia in 1..diasDelMes) add(mes.atDay(dia))
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(celdas) { fecha ->
            if (fecha == null) {
                Box(modifier = Modifier.aspectRatio(1f).padding(2.dp))
            } else {
                val turnosDia = turnosPorFecha[fecha.format(FORMATO_FECHA)] ?: emptyList()
                DiaCelda(
                    fecha = fecha,
                    turnos = turnosDia,
                    esHoy = fecha == hoy,
                    onClick = { onDiaClick(fecha) }
                )
            }
        }
    }
}

@Composable
private fun DiaCelda(
    fecha: LocalDate,
    turnos: List<Turno>,
    esHoy: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (esHoy) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = fecha.dayOfMonth.toString(),
                fontWeight = if (esHoy) FontWeight.ExtraBold else FontWeight.Normal,
                textDecoration = if (esHoy) TextDecoration.Underline else null
            )
            if (turnos.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    turnos.take(3).forEach { turno ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colorDeHex(turno.colorHex))
                        )
                    }
                }
                if (turnos.size > 3) {
                    Text("+${turnos.size - 3}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun Leyenda() {
    Text("Leyenda", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
    Column {
        TIPOS_TURNO_PREDEFINIDOS.chunked(2).forEach { fila ->
            Row(modifier = Modifier.fillMaxWidth()) {
                fila.forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp, bottom = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(colorDeHex(tipo.colorHex))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(tipo.nombre, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
