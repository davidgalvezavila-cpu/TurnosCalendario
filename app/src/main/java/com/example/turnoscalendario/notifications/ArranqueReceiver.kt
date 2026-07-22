package com.example.turnoscalendario.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.turnoscalendario.data.TurnoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * AlarmManager no conserva las alarmas programadas tras un reinicio del dispositivo,
 * así que al arrancar volvemos a programar los recordatorios de los turnos futuros.
 */
class ArranqueReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val contextoApp = context.applicationContext
        val resultadoPendiente = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = TurnoDatabase.obtenerInstancia(contextoApp).turnoDao()
                val hoy = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val turnos = dao.obtenerTurnosConRecordatorioDesde(hoy)
                turnos.forEach { turno -> RecordatorioScheduler.programar(contextoApp, turno) }
            } finally {
                resultadoPendiente.finish()
            }
        }
    }
}
