package com.example.turnoscalendario.calendarsync

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.example.turnoscalendario.data.Turno
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class CalendarioInfo(
    val id: Long,
    val nombre: String,
    val cuenta: String
)

/**
 * Sincroniza turnos con el Proveedor de Calendario de Android (CalendarContract) en lugar
 * de usar la API de Google Calendar (OAuth) directamente. Esto permite escribir eventos en
 * el calendario de Google del usuario (si lo elige) sin necesidad de configurar un proyecto
 * en Google Cloud Console, credenciales OAuth ni un flujo de inicio de sesión: el propio
 * sistema Android ya sincroniza ese calendario con la cuenta de Google en segundo plano.
 */
object GoogleCalendarSync {

    fun listarCalendarios(context: Context): List<CalendarioInfo> {
        val resultado = mutableListOf<CalendarioInfo>()
        val proyeccion = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )

        runCatching {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, proyeccion, null, null, null
            )?.use { cursor ->
                val idxId = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val idxNombre = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val idxCuenta = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                val idxNivel = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)

                while (cursor.moveToNext()) {
                    val nivelAcceso = cursor.getInt(idxNivel)
                    if (nivelAcceso >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                        resultado.add(
                            CalendarioInfo(
                                id = cursor.getLong(idxId),
                                nombre = cursor.getString(idxNombre) ?: "Sin nombre",
                                cuenta = cursor.getString(idxCuenta) ?: ""
                            )
                        )
                    }
                }
            }
        }

        return resultado
    }

    /** Crea o actualiza el evento de calendario asociado a un turno. Devuelve el id del evento, o null si no se pudo. */
    fun crearOActualizarEvento(context: Context, turno: Turno, calendarioId: Long): Long? {
        val horaInicio = turno.horaInicio ?: return null // turnos sin horario (ej. "Libre") no generan evento

        val fecha = runCatching { LocalDate.parse(turno.fecha) }.getOrNull() ?: return null
        val inicio = combinarFechaHora(fecha, horaInicio) ?: return null
        val fin = turno.horaFin?.let { combinarFechaHora(fecha, it) }
            ?.let { if (!it.isAfter(inicio)) it.plusDays(1) else it } // turno que cruza medianoche
            ?: inicio.plusHours(8)

        val zona = ZoneId.systemDefault()
        val valores = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarioId)
            put(CalendarContract.Events.TITLE, "Turno: ${turno.tipo}")
            put(CalendarContract.Events.DESCRIPTION, turno.notas ?: "")
            put(CalendarContract.Events.DTSTART, inicio.atZone(zona).toInstant().toEpochMilli())
            put(CalendarContract.Events.DTEND, fin.atZone(zona).toInstant().toEpochMilli())
            put(CalendarContract.Events.EVENT_TIMEZONE, zona.id)
        }

        return runCatching {
            val idExistente = turno.eventoCalendarioId
            if (idExistente != null) {
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, idExistente)
                val filasActualizadas = context.contentResolver.update(uri, valores, null, null)
                if (filasActualizadas > 0) idExistente else insertarNuevoEvento(context, valores)
            } else {
                insertarNuevoEvento(context, valores)
            }
        }.getOrNull()
    }

    fun eliminarEvento(context: Context, eventoId: Long) {
        runCatching {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventoId)
            context.contentResolver.delete(uri, null, null)
        }
    }

    private fun insertarNuevoEvento(context: Context, valores: ContentValues): Long? {
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, valores)
        return uri?.lastPathSegment?.toLongOrNull()
    }

    private fun combinarFechaHora(fecha: LocalDate, horaTexto: String): LocalDateTime? {
        val partes = horaTexto.split(":")
        if (partes.size != 2) return null
        val hora = partes[0].toIntOrNull() ?: return null
        val minuto = partes[1].toIntOrNull() ?: return null
        return fecha.atTime(hora, minuto)
    }
}
