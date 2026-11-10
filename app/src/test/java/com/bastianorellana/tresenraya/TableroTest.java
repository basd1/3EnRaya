package com.bastianorellana.tresenraya;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TableroTest {

    private Tablero t;

    @Before
    public void setUp() {
        t = new Tablero();
    }

    @Test
    public void tableroNuevo_sinGanadorNiLleno() {
        assertEquals(Tablero.VACIA, t.ganador());
        assertFalse(t.lleno());
    }

    @Test
    public void casillaOcupada_noSePuedeRepetir() {
        assertTrue(t.jugar(4, Tablero.J1));
        assertFalse(t.jugar(4, Tablero.J2));
        assertEquals(Tablero.J1, t.get(4));
    }

    @Test
    public void detectaTodasLasLineas() {
        int[][] lineas = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] linea : lineas) {
            t.reiniciar();
            for (int c : linea) t.jugar(c, Tablero.J2);
            assertEquals("Línea " + linea[0] + linea[1] + linea[2], Tablero.J2, t.ganador());
        }
    }

    @Test
    public void lineaMixta_noGana() {
        t.jugar(0, Tablero.J1);
        t.jugar(1, Tablero.J2);
        t.jugar(2, Tablero.J1);
        assertEquals(Tablero.VACIA, t.ganador());
    }

    @Test
    public void empate_tableroLlenoSinGanador() {
        // X O X
        // X O O
        // O X X
        int[] jugadas = {Tablero.J1, Tablero.J2, Tablero.J1,
                         Tablero.J1, Tablero.J2, Tablero.J2,
                         Tablero.J2, Tablero.J1, Tablero.J1};
        for (int i = 0; i < jugadas.length; i++) t.jugar(i, jugadas[i]);
        assertTrue(t.lleno());
        assertEquals(Tablero.VACIA, t.ganador());
    }

    @Test
    public void exportarEImportar_conservaElEstado() {
        t.jugar(0, Tablero.J1);
        t.jugar(8, Tablero.J2);
        Tablero otro = new Tablero();
        otro.importar(t.exportar());
        assertArrayEquals(t.exportar(), otro.exportar());
    }

    @Test
    public void importarEstadoInvalido_dejaTableroVacio() {
        t.jugar(0, Tablero.J1);
        t.importar(new int[]{1, 2});
        assertArrayEquals(new int[Tablero.NUM_CASILLAS], t.exportar());
    }
}
