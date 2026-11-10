package com.bastianorellana.tresenraya;

/** Foto inmutable de todo lo que la pantalla de juego necesita pintar. */
final class EstadoPartida {

    enum Resultado { EN_CURSO, GANA_J1, GANA_J2, EMPATE }

    final int[] casillas;
    final boolean turnoJ1;
    final Resultado resultado;
    /** Casillas de la línea ganadora, o null. */
    final int[] lineaGanadora;
    final boolean iaPensando;
    final boolean contraIa;
    final AlmacenPartida.Datos datos;

    EstadoPartida(int[] casillas, boolean turnoJ1, Resultado resultado, int[] lineaGanadora,
                  boolean iaPensando, boolean contraIa, AlmacenPartida.Datos datos) {
        this.casillas = casillas;
        this.turnoJ1 = turnoJ1;
        this.resultado = resultado;
        this.lineaGanadora = lineaGanadora;
        this.iaPensando = iaPensando;
        this.contraIa = contraIa;
        this.datos = datos;
    }

    boolean terminada() {
        return resultado != Resultado.EN_CURSO;
    }
}
