package com.example.turnoscalendario.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnoscalendario.calendarsync.GoogleCalendarSync
import com.example.turnoscalendario.data.PreferenciasManager
import com.example.turnoscalendario.data.Turno
import com.example.turnoscalendario.data.TurnoDatabase
import com.example.turnoscalendario.data.TurnoRepository
import com.example.turnoscalendario.notifications.RecordatorioScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class ModoVista { MENSUAL, SEMANAL, LISTA }

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio: TurnoRepository = TurnoRepository(
        TurnoDatabase.obtenerInstancia(application).turnoDao()
    )
    val preferencias = PreferenciasManager(application)

    private val formatoMes = DateTimeFormatter.ofPattern("yyyy-MM")

    private val _modoVista = MutableStateFlow(ModoVista.MENSUAL)
    val modoVista: StateFlow<ModoVista> = _modoVista

    private val _fechaReferencia = MutableStateFlow(LocalDate.now())
    val fechaReferencia: StateFlow<LocalDate> = _fechaReferencia

    private val _mesEstadisticas = MutableStateFlow(YearMonth.now())
    val mesEstadisticas: StateFlow<YearMonth> = _mesEstadisticas

    // Rango de fechas visible: mes completo o semana (lunes-domingo), según el modo activo.
    private val rangoVisible: StateFlow<Pair<LocalDate, LocalDate>> =
        combine(_fechaReferencia, _modoVista) { fecha, modo ->
            if (modo == ModoVista.SEMANAL) {
                val inicio = fecha.with(DayOfWeek.MONDAY)
                inicio to inicio.plusDays(6)
            } else {
                val mes = YearMonth.from(fecha)
                mes.atDay(1) to mes.atEndOfMonth()
            }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000),
            YearMonth.now().atDay(1) to YearMonth.now().atEndOfMonth()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val turnosVisibles: StateFlow<List<Turno>> = rangoVisible
        .flatMapLatest { (inicio, fin) ->
            repositorio.observarEnRango(inicio.format(FORMATO_FECHA), fin.format(FORMATO_FECHA))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosTurnos: StateFlow<List<Turno>> = repositorio.observarTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val estadisticasDelMes: StateFlow<EstadisticasMes> =
        combine(_mesEstadisticas, todosLosTurnos) { mes, turnos ->
            val prefijo = mes.format(formatoMes)
            calcularEstadisticas(turnos.filter { it.fecha.startsWith(prefijo) })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EstadisticasMes(0.0, emptyList(), 0))

    fun cambiarModoVista(modo: ModoVista) {
        _modoVista.value = modo
    }

    fun irAnterior() {
        _fechaReferencia.value = if (_modoVista.value == ModoVista.SEMANAL)
            _fechaReferencia.value.minusWeeks(1) else _fechaReferencia.value.minusMonths(1)
    }

    fun irSiguiente() {
        _fechaReferencia.value = if (_modoVista.value == ModoVista.SEMANAL)
            _fechaReferencia.value.plusWeeks(1) else _fechaReferencia.value.plusMonths(1)
    }

    fun irAHoy() {
        _fechaReferencia.value = LocalDate.now()
    }

    fun mesEstadisticasAnterior() {
        _mesEstadisticas.value = _mesEstadisticas.value.minusMonths(1)
    }

    fun mesEstadisticasSiguiente() {
        _mesEstadisticas.value = _mesEstadisticas.value.plusMonths(1)
    }

    fun guardarTurno(turno: Turno) {
        viewModelScope.launch {
            val guardado = repositorio.guardarTurno(turno)
            val app = getApplication<Application>()

            RecordatorioScheduler.cancelar(app, guardado.id)
            RecordatorioScheduler.programar(app, guardado)

            if (preferencias.sincronizacionActivada && preferencias.calendarioIdSeleccionado > 0) {
                val eventoId = withContext(Dispatchers.IO) {
                    GoogleCalendarSync.crearOActualizarEvento(app, guardado, preferencias.calendarioIdSeleccionado)
                }
                if (eventoId != null && eventoId != guardado.eventoCalendarioId) {
                    repositorio.guardarTurno(guardado.copy(eventoCalendarioId = eventoId))
                }
            }
        }
    }

    fun eliminarTurno(turno: Turno) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            RecordatorioScheduler.cancelar(app, turno.id)
            turno.eventoCalendarioId?.let { eventoId ->
                withContext(Dispatchers.IO) { GoogleCalendarSync.eliminarEvento(app, eventoId) }
            }
            repositorio.eliminarTurno(turno)
        }
    }

    fun sincronizarTodoConCalendario(alTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val calendarioId = preferencias.calendarioIdSeleccionado
            if (preferencias.sincronizacionActivada && calendarioId > 0) {
                val turnos = todosLosTurnos.value
                withContext(Dispatchers.IO) {
                    turnos.forEach { turno ->
                        val eventoId = GoogleCalendarSync.crearOActualizarEvento(app, turno, calendarioId)
                        if (eventoId != null && eventoId != turno.eventoCalendarioId) {
                            repositorio.guardarTurno(turno.copy(eventoCalendarioId = eventoId))
                        }
                    }
                }
            }
            alTerminar()
        }
    }
}
