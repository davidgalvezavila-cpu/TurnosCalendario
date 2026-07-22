package com.example.turnoscalendario.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val tipo = intent.getStringExtra(EXTRA_TIPO) ?: "Turno"
        val horaInicio = intent.getStringExtra(EXTRA_HORA_INICIO)
        val fecha = intent.getStringExtra(EXTRA_FECHA) ?: ""
        val turnoId = intent.getLongExtra(EXTRA_TURNO_ID, 0L)

        crearCanalNotificacion(context)

        val texto = if (horaInicio != null) "Empieza a las $horaInicio ($fecha)" else "Turno el $fecha"

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Turno: $tipo")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val tienePermiso = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            NotificationManagerCompat.from(context).notify(turnoId.toInt(), notificacion)
        }
    }

    companion object {
        const val EXTRA_TURNO_ID = "extra_turno_id"
        const val EXTRA_TIPO = "extra_tipo"
        const val EXTRA_HORA_INICIO = "extra_hora_inicio"
        const val EXTRA_FECHA = "extra_fecha"
        const val CANAL_ID = "canal_recordatorios_turnos"

        fun crearCanalNotificacion(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val canal = NotificationChannel(
                    CANAL_ID,
                    "Recordatorios de turnos",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisos antes de que empiece un turno laboral"
                }
                val manager = context.getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(canal)
            }
        }
    }
}
