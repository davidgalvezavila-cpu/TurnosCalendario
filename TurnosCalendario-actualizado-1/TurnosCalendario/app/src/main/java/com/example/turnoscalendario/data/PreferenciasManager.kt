package com.example.turnoscalendario.data

import android.content.Context

/**
 * Envoltorio simple sobre SharedPreferences para guardar los ajustes de la app:
 * minutos de recordatorio por defecto y configuración de sincronización con el calendario.
 */
class PreferenciasManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)

    var recordatorioMinutosPorDefecto: Int
        get() = prefs.getInt(KEY_RECORDATORIO_DEFECTO, 30)
        set(value) = prefs.edit().putInt(KEY_RECORDATORIO_DEFECTO, value).apply()

    var sincronizacionActivada: Boolean
        get() = prefs.getBoolean(KEY_SYNC_ACTIVADA, false)
        set(value) = prefs.edit().putBoolean(KEY_SYNC_ACTIVADA, value).apply()

    var calendarioIdSeleccionado: Long
        get() = prefs.getLong(KEY_CALENDARIO_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_CALENDARIO_ID, value).apply()

    companion object {
        private const val NOMBRE_PREFS = "turnos_prefs"
        private const val KEY_RECORDATORIO_DEFECTO = "recordatorio_minutos_defecto"
        private const val KEY_SYNC_ACTIVADA = "sync_activada"
        private const val KEY_CALENDARIO_ID = "calendario_id"
    }
}
