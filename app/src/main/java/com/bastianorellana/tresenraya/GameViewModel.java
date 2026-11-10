package com.bastianorellana.tresenraya;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

/**
 * Lógica de la pantalla de juego. Sobrevive a los giros de pantalla y, gracias a
 * {@link SavedStateHandle}, también a que el sistema mate el proceso en segundo plano.
 * Nombres, colores y marcador se guardan entre sesiones en {@link AlmacenPartida}.
 */
public class GameViewModel extends ViewModel {

    static final long RETRASO_IA_MS = 600;

    private static final String KEY_TABLERO = "tablero";
    private static final String KEY_TURNO_J1 = "turno_j1";

    private final SavedStateHandle estadoGuardado;
    private final AlmacenPartida almacen;
    private final Programador programador;
    private final Ia ia;
    /** null en partidas de dos jugadores; la IA siempre es J2. */
    @Nullable private final Ia.Dificultad dificultad;
    private final String claveAlmacen;

    private final Tablero tablero = new Tablero();
    private boolean turnoJ1;
    private boolean iaPensando;
    private AlmacenPartida.Datos datos;

    private final MutableLiveData<EstadoPartida> estado = new MutableLiveData<>();
    private final MutableLiveData<Evento> eventos = new MutableLiveData<>();

    GameViewModel(@NonNull SavedStateHandle estadoGuardado,
                  @NonNull AlmacenPartida almacen,
                  @NonNull Programador programador,
                  @NonNull Ia ia,
                  @Nullable Ia.Dificultad dificultad,
                  @NonNull AlmacenPartida.Datos porDefecto) {
        this.estadoGuardado = estadoGuardado;
        this.almacen = almacen;
        this.programador = programador;
        this.ia = ia;
        this.dificultad = dificultad;
        this.claveAlmacen = dificultad == null ? "local" : "ia";

        datos = almacen.cargar(claveAlmacen, porDefecto);
        tablero.importar(estadoGuardado.get(KEY_TABLERO));
        Boolean turno = estadoGuardado.get(KEY_TURNO_J1);
        turnoJ1 = turno == null || turno;

        publicar();
        programarIaSiLeToca();
    }

    LiveData<EstadoPartida> getEstado() {
        return estado;
    }

    LiveData<Evento> getEventos() {
        return eventos;
    }

    private boolean contraIa() {
        return dificultad != null;
    }

    // ------------------------------------------------------------------ Acciones

    void pulsarCasilla(int casilla) {
        if (tablero.terminado() || iaPensando || (contraIa() && !turnoJ1)) return;
        jugar(casilla);
        programarIaSiLeToca();
    }

    /** Partida nueva conservando el marcador. */
    void otraPartida() {
        programador.cancelarTodo();
        iaPensando = false;
        tablero.reiniciar();
        publicar();
        programarIaSiLeToca();
    }

    /** Partida nueva con el marcador a cero; empieza J1. */
    void reiniciarMarcador() {
        datos.puntos1 = 0;
        datos.puntos2 = 0;
        turnoJ1 = true;
        guardarDatos();
        otraPartida();
    }

    /** objetivo: 1 = jugador 1, 2 = jugador 2, 3 = título de la partida. */
    void cambiarNombre(int objetivo, String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) return; // un nombre vacío dejaría el texto invisible
        if (objetivo == 1) datos.nombre1 = limpio;
        else if (objetivo == 2) datos.nombre2 = limpio;
        else datos.titulo = limpio;
        guardarDatos();
        publicar();
    }

    void cambiarColor(int jugador, int color) {
        if (jugador == 1) datos.color1 = color;
        else datos.color2 = color;
        guardarDatos();
        publicar();
    }

    // ------------------------------------------------------------------ Lógica interna

    private void jugar(int casilla) {
        int jugador = turnoJ1 ? Tablero.J1 : Tablero.J2;
        if (!tablero.jugar(casilla, jugador)) {
            eventos.setValue(new Evento(Evento.Tipo.CASILLA_OCUPADA, casilla, resultado()));
            return;
        }

        EstadoPartida.Resultado resultado = resultado();
        if (resultado == EstadoPartida.Resultado.GANA_J1) datos.puntos1++;
        else if (resultado == EstadoPartida.Resultado.GANA_J2) datos.puntos2++;
        if (resultado != EstadoPartida.Resultado.EN_CURSO) guardarDatos();

        // El turno alterna siempre, así la siguiente partida la empieza el otro jugador
        turnoJ1 = !turnoJ1;
        publicar();
        eventos.setValue(new Evento(Evento.Tipo.JUGADA, casilla, resultado));
    }

    private void programarIaSiLeToca() {
        if (!contraIa() || turnoJ1 || tablero.terminado() || iaPensando) return;
        iaPensando = true;
        publicar();
        programador.programar(() -> {
            iaPensando = false;
            jugar(ia.elegirCasilla(tablero, Tablero.J2, dificultad));
        }, RETRASO_IA_MS);
    }

    private EstadoPartida.Resultado resultado() {
        int ganador = tablero.ganador();
        if (ganador == Tablero.J1) return EstadoPartida.Resultado.GANA_J1;
        if (ganador == Tablero.J2) return EstadoPartida.Resultado.GANA_J2;
        if (tablero.lleno()) return EstadoPartida.Resultado.EMPATE;
        return EstadoPartida.Resultado.EN_CURSO;
    }

    private void guardarDatos() {
        almacen.guardar(claveAlmacen, datos);
    }

    private void publicar() {
        estadoGuardado.set(KEY_TABLERO, tablero.exportar());
        estadoGuardado.set(KEY_TURNO_J1, turnoJ1);
        estado.setValue(new EstadoPartida(
                tablero.exportar(), turnoJ1, resultado(), tablero.lineaGanadora(),
                iaPensando, contraIa(), datos.copia()));
    }

    @Override
    protected void onCleared() {
        programador.cancelarTodo();
    }
}
