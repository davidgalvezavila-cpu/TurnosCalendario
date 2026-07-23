package com.example.turnoscalendario.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Query("SELECT * FROM turnos ORDER BY fecha ASC, horaInicio ASC")
    fun observarTodos(): Flow<List<Turno>>

    // Usado por las vistas de mes y semana: solo pedimos los turnos del rango visible.
    @Query("SELECT * FROM turnos WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha ASC, horaInicio ASC")
    fun observarEnRango(inicio: String, fin: String): Flow<List<Turno>>

    @Query("SELECT * FROM turnos WHERE fecha = :fecha ORDER BY horaInicio ASC")
    suspend fun obtenerPorFecha(fecha: String): List<Turno>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(turno: Turno): Long

    @Update
    suspend fun actualizar(turno: Turno)

    @Delete
    suspend fun eliminar(turno: Turno)

    // Usado al reiniciar el dispositivo para volver a programar los recordatorios futuros.
    @Query("SELECT * FROM turnos WHERE recordatorioMinutosAntes IS NOT NULL AND fecha >= :fechaDesde")
    suspend fun obtenerTurnosConRecordatorioDesde(fechaDesde: String): List<Turno>
}
