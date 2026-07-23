package com.example.turnoscalendario.data

import kotlinx.coroutines.flow.Flow

class TurnoRepository(private val dao: TurnoDao) {

    fun observarTodos(): Flow<List<Turno>> = dao.observarTodos()

    fun observarEnRango(inicio: String, fin: String): Flow<List<Turno>> = dao.observarEnRango(inicio, fin)

    /** Inserta si es un turno nuevo (id == 0) o actualiza el existente. Devuelve el turno ya con id. */
    suspend fun guardarTurno(turno: Turno): Turno {
        val id = if (turno.id == 0L) {
            dao.insertar(turno)
        } else {
            dao.actualizar(turno)
            turno.id
        }
        return turno.copy(id = id)
    }

    suspend fun eliminarTurno(turno: Turno) {
        dao.eliminar(turno)
    }

    suspend fun obtenerTurnosDelDia(fecha: String): List<Turno> = dao.obtenerPorFecha(fecha)

    suspend fun obtenerTurnosConRecordatorioDesde(fechaDesde: String): List<Turno> =
        dao.obtenerTurnosConRecordatorioDesde(fechaDesde)
}
