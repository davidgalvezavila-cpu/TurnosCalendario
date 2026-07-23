package com.example.turnoscalendario.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Turno::class], version = 2, exportSchema = false)
abstract class TurnoDatabase : RoomDatabase() {

    abstract fun turnoDao(): TurnoDao

    companion object {
        @Volatile
        private var INSTANCIA: TurnoDatabase? = null

        // Migración de la v1 (calendario simple) a la v2 (recordatorios + sync con calendario).
        // No hace falta tocar nada relacionado con "un turno por día": esa restricción nunca
        // existió a nivel de base de datos, siempre se gestionó en el código del repositorio.
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE turnos ADD COLUMN recordatorioMinutosAntes INTEGER")
                db.execSQL("ALTER TABLE turnos ADD COLUMN eventoCalendarioId INTEGER")
            }
        }

        fun obtenerInstancia(context: Context): TurnoDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    TurnoDatabase::class.java,
                    "turnos_db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
