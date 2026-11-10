package com.bastianorellana.tresenraya;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GameViewModelTest {

    @Rule
    public InstantTaskExecutorRule liveDataInstantanea = new InstantTaskExecutorRule();

    private AlmacenFalso almacen;
    private ProgramadorManual programador;
    private SavedStateHandle estadoGuardado;

    @Before
    public void setUp() {
        almacen = new AlmacenFalso();
        programador = new ProgramadorManual();
        estadoGuardado = new SavedStateHandle();
    }

    private GameViewModel crear(Ia.Dificultad dificultad) {
        return new GameViewModel(estadoGuardado, almacen, programador, new Ia(new Random(1)),
                dificultad, porDefecto());
    }

    private static AlmacenPartida.Datos porDefecto() {
        AlmacenPartida.Datos d = new AlmacenPartida.Datos();
        d.titulo = "Partida";
        d.nombre1 = "J1";
        d.nombre2 = "J2";
        d.color1 = 1;
        d.color2 = 2;
        return d;
    }

    private static EstadoPartida estado(GameViewModel vm) {
        return vm.getEstado().getValue();
    }

    private static void jugar(GameViewModel vm, int... casillas) {
        for (int c : casillas) vm.pulsarCasilla(c);
    }

    // ---------------------------------------------------------------- Dos jugadores

    @Test
    public void victoriaJ1_sumaPuntoYMarcaLinea() {
        GameViewModel vm = crear(null);
        jugar(vm, 0, 3, 1, 4, 2);

        EstadoPartida e = estado(vm);
        assertEquals(EstadoPartida.Resultado.GANA_J1, e.resultado);
        assertArrayEquals(new int[]{0, 1, 2}, e.lineaGanadora);
        assertEquals(1, e.datos.puntos1);
        assertEquals(1, almacen.guardado("local").puntos1);
    }

    @Test
    public void partidaTerminada_ignoraMasJugadas() {
        GameViewModel vm = crear(null);
        jugar(vm, 0, 3, 1, 4, 2);
        vm.pulsarCasilla(8);
        assertEquals(Tablero.VACIA, estado(vm).casillas[8]);
    }

    @Test
    public void empate_noSumaPuntos() {
        GameViewModel vm = crear(null);
        // X O X / X O O / O X X
        jugar(vm, 0, 1, 2, 4, 3, 5, 7, 6, 8);
        EstadoPartida e = estado(vm);
        assertEquals(EstadoPartida.Resultado.EMPATE, e.resultado);
        assertNull(e.lineaGanadora);
        assertEquals(0, e.datos.puntos1 + e.datos.puntos2);
    }

    @Test
    public void casillaOcupada_emiteEventoYNoCambiaTurno() {
        GameViewModel vm = crear(null);
        vm.pulsarCasilla(4);
        vm.pulsarCasilla(4);
        assertEquals(Evento.Tipo.CASILLA_OCUPADA, vm.getEventos().getValue().tipo);
        assertFalse(estado(vm).turnoJ1); // sigue siendo el turno de J2
    }

    @Test
    public void otraPartida_conservaMarcadorYAlternaQuienEmpieza() {
        GameViewModel vm = crear(null);
        jugar(vm, 0, 3, 1, 4, 2); // gana J1 con la 5ª jugada; ahora le toca a J2
        vm.otraPartida();
        EstadoPartida e = estado(vm);
        assertArrayEquals(new int[Tablero.NUM_CASILLAS], e.casillas);
        assertEquals(1, e.datos.puntos1);
        assertFalse(e.turnoJ1);
    }

    @Test
    public void reiniciarMarcador_poneACeroYEmpiezaJ1() {
        GameViewModel vm = crear(null);
        jugar(vm, 0, 3, 1, 4, 2);
        vm.reiniciarMarcador();
        EstadoPartida e = estado(vm);
        assertEquals(0, e.datos.puntos1);
        assertTrue(e.turnoJ1);
    }

    @Test
    public void cambiarNombre_vacioSeIgnora() {
        GameViewModel vm = crear(null);
        vm.cambiarNombre(1, "   ");
        assertEquals("J1", estado(vm).datos.nombre1);
        vm.cambiarNombre(1, "  Bas ");
        assertEquals("Bas", estado(vm).datos.nombre1);
        assertEquals("Bas", almacen.guardado("local").nombre1);
    }

    @Test
    public void recreado_recuperaTableroYTurno() {
        GameViewModel vm = crear(null);
        jugar(vm, 4, 0);
        GameViewModel recreado = crear(null); // mismo SavedStateHandle, como tras matar el proceso
        EstadoPartida e = estado(recreado);
        assertEquals(Tablero.J1, e.casillas[4]);
        assertEquals(Tablero.J2, e.casillas[0]);
        assertTrue(e.turnoJ1);
    }

    @Test
    public void datosGuardados_seCarganAlCrear() {
        AlmacenPartida.Datos d = porDefecto();
        d.puntos2 = 7;
        almacen.guardar("local", d);
        assertEquals(7, estado(crear(null)).datos.puntos2);
    }

    // ---------------------------------------------------------------- Contra la IA

    @Test
    public void ia_respondeTrasElRetraso() {
        GameViewModel vm = crear(Ia.Dificultad.DIFICIL);
        vm.pulsarCasilla(0);
        assertTrue(estado(vm).iaPensando);
        assertEquals(GameViewModel.RETRASO_IA_MS, programador.ultimoRetraso);

        programador.ejecutarPendientes();
        EstadoPartida e = estado(vm);
        assertFalse(e.iaPensando);
        assertTrue(e.turnoJ1);
        assertEquals(7, contar(e.casillas, Tablero.VACIA));
    }

    @Test
    public void ia_noDejaJugarMientrasPiensa() {
        GameViewModel vm = crear(Ia.Dificultad.FACIL);
        vm.pulsarCasilla(0);
        vm.pulsarCasilla(1); // la IA aún no ha jugado: se ignora
        assertEquals(Tablero.VACIA, estado(vm).casillas[1]);
    }

    @Test
    public void ia_empiezaSiLeTocaEnLaSiguientePartida() {
        GameViewModel vm = crear(Ia.Dificultad.DIFICIL);
        // J1 empieza; tras una partida de jugadas impares, empieza la IA
        vm.pulsarCasilla(4);
        programador.ejecutarPendientes();
        vm.otraPartida(); // turno: J1 (se alternó dos veces)
        assertTrue(estado(vm).turnoJ1);
        assertFalse(estado(vm).iaPensando);

        vm.pulsarCasilla(4);
        vm.otraPartida(); // se cancela la jugada pendiente de la IA y le toca a ella empezar
        assertTrue(estado(vm).iaPensando);
        programador.ejecutarPendientes();
        assertEquals(8, contar(estado(vm).casillas, Tablero.VACIA));
    }

    @Test
    public void ia_usaSuPropioMarcador() {
        GameViewModel vm = crear(Ia.Dificultad.FACIL);
        vm.cambiarNombre(1, "Bas");
        assertEquals("Bas", almacen.guardado("ia").nombre1);
        assertNull(almacen.guardado("local"));
    }

    private static int contar(int[] valores, int buscado) {
        int n = 0;
        for (int v : valores) if (v == buscado) n++;
        return n;
    }

    // ---------------------------------------------------------------- Dobles de prueba

    private static final class AlmacenFalso implements AlmacenPartida {
        private final Map<String, Datos> datos = new HashMap<>();

        @Override
        public Datos cargar(String clave, Datos porDefecto) {
            Datos d = datos.get(clave);
            return (d == null ? porDefecto : d).copia();
        }

        @Override
        public void guardar(String clave, Datos d) {
            datos.put(clave, d.copia());
        }

        Datos guardado(String clave) {
            return datos.get(clave);
        }
    }

    /** Guarda las tareas y solo las ejecuta cuando el test lo pide. */
    private static final class ProgramadorManual implements Programador {
        private final List<Runnable> pendientes = new ArrayList<>();
        long ultimoRetraso;

        @Override
        public void programar(Runnable tarea, long retrasoMs) {
            ultimoRetraso = retrasoMs;
            pendientes.add(tarea);
        }

        @Override
        public void cancelarTodo() {
            pendientes.clear();
        }

        void ejecutarPendientes() {
            List<Runnable> copia = new ArrayList<>(pendientes);
            pendientes.clear();
            for (Runnable r : copia) r.run();
        }
    }
}
