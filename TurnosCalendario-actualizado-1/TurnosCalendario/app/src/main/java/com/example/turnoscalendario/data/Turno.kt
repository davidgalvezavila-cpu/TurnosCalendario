package com.example.turnoscalendario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un turno laboral asignado a un día concreto.
 * [fecha] se guarda en formato "yyyy-MM-dd" para poder indexarla y compararla fácilmente.
 *
 * IMPORTANTE: no existe restricción de "un turno por día" a nivel de base de datos
 * (nunca existió), así que un mismo [fecha] puede tener varias filas: esto es lo que
 * permite los turnos partidos (ej. Mañana + Tarde el mismo día).
 */
@Entity(tableName = "turnos")
data class Turno(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: String,          // yyyy-MM-dd
    val tipo: String,           // Nombre del tipo de turno (Mañana, Tarde, Noche, Libre, etc.)
    val colorHex: String,       // Color asociado en formato #RRGGBB
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val notas: String? = null,
    val recordatorioMinutosAntes: Int? = null, // null = sin recordatorio
    val eventoCalendarioId: Long? = null       // id del evento espejo en CalendarContract (si hay sync activada)
)

/**
 * Tipos de turno predefinidos. El usuario puede editar horario/tipo libremente al crear
 * un turno; esto solo sirve como plantilla rápida.
 */
data class TipoTurno(
    val nombre: String,
    val colorHex: String,
    val horaInicio: String,
    val horaFin: String
)

val TIPOS_TURNO_PREDEFINIDOS = listOf(
    TipoTurno("Mañana", "#4CAF50", "06:00", "14:00"),
    TipoTurno("Tarde", "#2196F3", "14:00", "22:00"),
    TipoTurno("Noche", "#673AB7", "22:00", "06:00"),
    TipoTurno("Libre", "#9E9E9E", "-", "-"),
    TipoTurno("Vacaciones", "#FF9800", "-", "-")
)
