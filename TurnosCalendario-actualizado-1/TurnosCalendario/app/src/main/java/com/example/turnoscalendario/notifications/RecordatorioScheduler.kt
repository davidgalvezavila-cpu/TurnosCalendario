package com.example.turnoscalendario.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.turnoscalendario.data.Turno
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Programa y cancela los recordatorios (avisos) antes de que empiece un turno, usando
 * AlarmManager. Se usan alarmas "inexactas conscientes de Doze" (setAndAllowWhileIdle),
 * que no requieren el permiso especial de alarmas exactas (SCHEDULE_EXACT_ALARM): el
 * aviso puede llegar con algunos minutos de margen en dispositivos con ahorro de batería
 * agresivo, lo cual es un compromiso razonable para este caso de uso.
 */
object RecordatorioScheduler {

    fun programar(context: Context, turno: Turno) {
        val minutosAntes = turno.recordatorioMinutosAntes ?: return
        val horaInicio = turno.horaInicio ?: return
        if (turno.id == 0L) return

        val partes = horaInicio.split(":")
        if (partes.size != 2) return
        val hora = partes[0].toIntOrNull() ?: return
        val minuto = partes[1].toIntOrNull() ?: return

        val fecha = runCatching { LocalDate.parse(turno.fecha) }.getOrNull() ?: return
        val momentoDisparo = fecha.atTime(hora, minuto).minusMinutes(minutosAntes.toLong())

        // Si el momento ya pasó (ej. turno de hoy que ya empezó), no programamos nada.
        if (momentoDisparo.isBefore(LocalDateTime.now())) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = crearPendingIntent(context, turno)
        val trigger = momentoDisparo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent)
    }

    fun cancelar(context: Context, turnoId: Long) {
        if (turnoId == 0L) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, turnoId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun crearPendingIntent(context: Context, turno: Turno): PendingIntent {
        val intent = Intent(context, RecordatorioReceiver::class.java).apply {
            putExtra(RecordatorioReceiver.EXTRA_TURNO_ID, turno.id)
            putExtra(RecordatorioReceiver.EXTRA_TIPO, turno.tipo)
            putExtra(RecordatorioReceiver.EXTRA_HORA_INICIO, turno.horaInicio)
            putExtra(RecordatorioReceiver.EXTRA_FECHA, turno.fecha)
        }
        return PendingIntent.getBroadcast(
            context, turno.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
