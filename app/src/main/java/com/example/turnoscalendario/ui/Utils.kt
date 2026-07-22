package com.example.turnoscalendario.ui

import androidx.compose.ui.graphics.Color
import com.example.turnoscalendario.data.Turno
import java.time.format.DateTimeFormatter

val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun colorDeHex(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrElse { Color.Gray }

fun esHoraValida(texto: String): Boolean =
    Regex("^([01]\\d|2[0-3]):([0-5]\\d)$").matches(texto)

/** Duración en horas de un turno, teniendo en cuenta turnos nocturnos que cruzan la medianoche. */
fun calcularDuracionHoras(turno: Turno): Double {
    val inicio = turno.horaInicio ?: return 0.0
    val fin = turno.horaFin ?: return 0.0
    return runCatching {
        val (h1, m1) = inicio.split(":").map { it.toInt() }
        val (h2, m2) = fin.split(":").map { it.toInt() }
        val minutosInicio = h1 * 60 + m1
        var minutosFin = h2 * 60 + m2
        if (minutosFin <= minutosInicio) minutosFin += 24 * 60
        (minutosFin - minutosInicio) / 60.0
    }.getOrElse { 0.0 }
}

fun formatoHoras(horas: Double): String {
    val h = horas.toInt()
    val minutos = ((horas - h) * 60).toInt()
    return if (minutos == 0) "$h h" else "$h h $minutos min"
}

data class EstadisticaTipo(
    val tipo: String,
    val colorHex: String,
    val horas: Double,
    val turnos: Int
)

data class EstadisticasMes(
    val horasTotales: Double,
    val porTipo: List<EstadisticaTipo>,
    val diasConTurno: Int
)

fun calcularEstadisticas(turnos: List<Turno>): EstadisticasMes {
    val porTipo = turnos.groupBy { it.tipo }.map { (tipo, lista) ->
        EstadisticaTipo(
            tipo = tipo,
            colorHex = lista.first().colorHex,
            horas = lista.sumOf { calcularDuracionHoras(it) },
            turnos = lista.size
        )
    }.sortedByDescending { it.horas }

    return EstadisticasMes(
        horasTotales = porTipo.sumOf { it.horas },
        porTipo = porTipo,
        diasConTurno = turnos.map { it.fecha }.distinct().size
    )
}
