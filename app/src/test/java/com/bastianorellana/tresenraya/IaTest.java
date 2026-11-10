package com.bastianorellana.tresenraya;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class IaTest {

    private final Ia ia = new Ia(new Random(42));

    @Test
    public void facil_siempreEligeCasillaLibre() {
        Tablero t = new Tablero();
        t.jugar(0, Tablero.J1);
        t.jugar(4, Tablero.J2);
        for (int i = 0; i < 100; i++) {
            int c = ia.elegirCasilla(t, Tablero.J2, Ia.Dificultad.FACIL);
            assertEquals(Tablero.VACIA, t.get(c));
        }
    }

    @Test
    public void media_ganaSiPuede() {
        // O O .   la IA (J2) debe completar la fila
        // X X .
        Tablero t = new Tablero();
        t.jugar(3, Tablero.J1);
        t.jugar(0, Tablero.J2);
        t.jugar(4, Tablero.J1);
        t.jugar(1, Tablero.J2);
        assertEquals(2, ia.elegirCasilla(t, Tablero.J2, Ia.Dificultad.MEDIA));
    }

    @Test
    public void media_bloqueaAlRival() {
        // X X .   la IA (J2) debe tapar la casilla 2
        // . O .
        Tablero t = new Tablero();
        t.jugar(0, Tablero.J1);
        t.jugar(4, Tablero.J2);
        t.jugar(1, Tablero.J1);
        assertEquals(2, ia.elegirCasilla(t, Tablero.J2, Ia.Dificultad.MEDIA));
    }

    @Test
    public void dificil_prefiereGanarABloquear() {
        // X X .
        // O O .   puede bloquear en 2 o ganar en 5: debe ganar
        // X . .
        Tablero t = new Tablero();
        t.jugar(0, Tablero.J1);
        t.jugar(3, Tablero.J2);
        t.jugar(1, Tablero.J1);
        t.jugar(4, Tablero.J2);
        t.jugar(6, Tablero.J1);
        assertEquals(5, ia.elegirCasilla(t, Tablero.J2, Ia.Dificultad.DIFICIL));
    }

    /** Recorre TODAS las partidas posibles del rival: la IA difícil nunca debe perder. */
    @Test
    public void dificil_nuncaPierde_empezandoElRival() {
        assertNuncaPierde(new Tablero(), Tablero.J1);
    }

    @Test
    public void dificil_nuncaPierde_empezandoLaIa() {
        assertNuncaPierde(new Tablero(), Tablero.J2);
    }

    private void assertNuncaPierde(Tablero t, int turno) {
        if (t.terminado()) {
            assertNotEquals("La IA ha perdido en:\n" + dibujar(t), Tablero.J1, t.ganador());
            return;
        }
        if (turno == Tablero.J2) {
            Tablero siguiente = t.copia();
            assertTrue(siguiente.jugar(ia.elegirCasilla(t, Tablero.J2, Ia.Dificultad.DIFICIL), Tablero.J2));
            assertNuncaPierde(siguiente, Tablero.J1);
        } else {
            for (int c : t.casillasLibres()) {
                Tablero siguiente = t.copia();
                siguiente.jugar(c, Tablero.J1);
                assertNuncaPierde(siguiente, Tablero.J2);
            }
        }
    }

    private static String dibujar(Tablero t) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Tablero.NUM_CASILLAS; i++) {
            sb.append(t.get(i) == Tablero.J1 ? 'X' : t.get(i) == Tablero.J2 ? 'O' : '.');
            if (i % 3 == 2) sb.append('\n');
        }
        return sb.toString();
    }
}
