package com.bastianorellana.tresenraya;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Estado y reglas del tres en raya, sin dependencias de Android.
 * Las casillas se numeran de 0 a 8, de izquierda a derecha y de arriba abajo.
 */
final class Tablero {

    static final int VACIA = 0;
    static final int J1 = 1;
    static final int J2 = 2;
    static final int NUM_CASILLAS = 9;

    private static final int[][] LINEAS = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // filas
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columnas
            {0, 4, 8}, {2, 4, 6}             // diagonales
    };

    private final int[] casillas = new int[NUM_CASILLAS];

    static int rival(int jugador) {
        return jugador == J1 ? J2 : J1;
    }

    int get(int casilla) {
        return casillas[casilla];
    }

    /** Marca la casilla para el jugador. Devuelve false si ya estaba ocupada. */
    boolean jugar(int casilla, int jugador) {
        if (jugador != J1 && jugador != J2) {
            throw new IllegalArgumentException("Jugador no válido: " + jugador);
        }
        if (casillas[casilla] != VACIA) {
            return false;
        }
        casillas[casilla] = jugador;
        return true;
    }

    /** Devuelve las tres casillas de la línea ganadora, o null si nadie ha ganado. */
    int[] lineaGanadora() {
        for (int[] linea : LINEAS) {
            int c = casillas[linea[0]];
            if (c != VACIA && c == casillas[linea[1]] && c == casillas[linea[2]]) {
                return linea.clone();
            }
        }
        return null;
    }

    /** Devuelve J1 o J2 si alguno tiene tres en raya, o VACIA si no hay ganador. */
    int ganador() {
        int[] linea = lineaGanadora();
        return linea == null ? VACIA : casillas[linea[0]];
    }

    boolean lleno() {
        for (int c : casillas) {
            if (c == VACIA) return false;
        }
        return true;
    }

    boolean terminado() {
        return ganador() != VACIA || lleno();
    }

    List<Integer> casillasLibres() {
        List<Integer> libres = new ArrayList<>();
        for (int i = 0; i < NUM_CASILLAS; i++) {
            if (casillas[i] == VACIA) libres.add(i);
        }
        return libres;
    }

    Tablero copia() {
        Tablero t = new Tablero();
        System.arraycopy(casillas, 0, t.casillas, 0, NUM_CASILLAS);
        return t;
    }

    void reiniciar() {
        Arrays.fill(casillas, VACIA);
    }

    int[] exportar() {
        return casillas.clone();
    }

    void importar(int[] estado) {
        if (estado == null || estado.length != NUM_CASILLAS) {
            reiniciar();
            return;
        }
        System.arraycopy(estado, 0, casillas, 0, NUM_CASILLAS);
    }
}
