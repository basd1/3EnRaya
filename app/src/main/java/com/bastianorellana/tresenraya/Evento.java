package com.bastianorellana.tresenraya;

/**
 * Acción puntual que la pantalla debe reaccionar una sola vez (animación, vibración,
 * celebración...). Al girar la pantalla no se repite gracias a {@link #consumir()}.
 */
final class Evento {

    enum Tipo { JUGADA, CASILLA_OCUPADA }

    final Tipo tipo;
    final int casilla;
    final EstadoPartida.Resultado resultado;
    private boolean consumido;

    Evento(Tipo tipo, int casilla, EstadoPartida.Resultado resultado) {
        this.tipo = tipo;
        this.casilla = casilla;
        this.resultado = resultado;
    }

    /** Devuelve true solo la primera vez que se llama. */
    boolean consumir() {
        if (consumido) return false;
        consumido = true;
        return true;
    }
}
