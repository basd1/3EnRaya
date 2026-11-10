package com.bastianorellana.tresenraya;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rival controlado por la máquina.
 * <ul>
 *   <li>FÁCIL: juega en una casilla libre al azar.</li>
 *   <li>MEDIA: gana si puede, si no bloquea al rival, si no prefiere el centro y después el azar.</li>
 *   <li>DIFÍCIL: minimax completo; nunca pierde.</li>
 * </ul>
 */
final class Ia {

    enum Dificultad { FACIL, MEDIA, DIFICIL }

    private final Random random;

    Ia(Random random) {
        this.random = random;
    }

    int elegirCasilla(Tablero tablero, int jugadorIa, Dificultad dificultad) {
        List<Integer> libres = tablero.casillasLibres();
        if (libres.isEmpty() || tablero.ganador() != Tablero.VACIA) {
            throw new IllegalStateException("La partida ya ha terminado");
        }
        switch (dificultad) {
            case FACIL:
                return alAzar(libres);
            case MEDIA:
                return jugadaMedia(tablero, jugadorIa, libres);
            case DIFICIL:
            default:
                return mejorJugada(tablero, jugadorIa);
        }
    }

    private int alAzar(List<Integer> opciones) {
        return opciones.get(random.nextInt(opciones.size()));
    }

    private int jugadaMedia(Tablero tablero, int jugadorIa, List<Integer> libres) {
        Integer ganadora = jugadaQueGana(tablero, jugadorIa, libres);
        if (ganadora != null) return ganadora;

        Integer bloqueo = jugadaQueGana(tablero, Tablero.rival(jugadorIa), libres);
        if (bloqueo != null) return bloqueo;

        if (libres.contains(4)) return 4;
        return alAzar(libres);
    }

    private static Integer jugadaQueGana(Tablero tablero, int jugador, List<Integer> libres) {
        for (int casilla : libres) {
            Tablero prueba = tablero.copia();
            prueba.jugar(casilla, jugador);
            if (prueba.ganador() == jugador) return casilla;
        }
        return null;
    }

    /** Minimax: elige al azar entre las jugadas con mejor puntuación, para no repetirse. */
    private int mejorJugada(Tablero tablero, int jugadorIa) {
        int mejorPuntuacion = Integer.MIN_VALUE;
        List<Integer> mejores = new ArrayList<>();
        for (int casilla : tablero.casillasLibres()) {
            Tablero siguiente = tablero.copia();
            siguiente.jugar(casilla, jugadorIa);
            int puntuacion = minimax(siguiente, jugadorIa, Tablero.rival(jugadorIa), 1);
            if (puntuacion > mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejores.clear();
                mejores.add(casilla);
            } else if (puntuacion == mejorPuntuacion) {
                mejores.add(casilla);
            }
        }
        return alAzar(mejores);
    }

    /**
     * Puntuación desde el punto de vista de la IA. La profundidad hace que prefiera
     * ganar cuanto antes y, si va a perder, tardar lo máximo posible.
     */
    private static int minimax(Tablero tablero, int jugadorIa, int turno, int profundidad) {
        int ganador = tablero.ganador();
        if (ganador == jugadorIa) return 10 - profundidad;
        if (ganador != Tablero.VACIA) return profundidad - 10;
        if (tablero.lleno()) return 0;

        boolean maximiza = turno == jugadorIa;
        int mejor = maximiza ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int casilla : tablero.casillasLibres()) {
            Tablero siguiente = tablero.copia();
            siguiente.jugar(casilla, turno);
            int valor = minimax(siguiente, jugadorIa, Tablero.rival(turno), profundidad + 1);
            mejor = maximiza ? Math.max(mejor, valor) : Math.min(mejor, valor);
        }
        return mejor;
    }
}
