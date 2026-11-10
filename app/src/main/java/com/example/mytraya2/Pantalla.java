package com.example.mytraya2;

import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Utilidades de ventana compartidas por las pantallas del juego. */
final class Pantalla {

    private Pantalla() {
    }

    /**
     * Pantalla completa a sangre (edge-to-edge, obligatorio desde Android 15) con la barra
     * de estado oculta. El fondo de {@code raiz} llega a los bordes, pero su contenido se
     * separa de la barra de navegación y del notch para que no quede tapado.
     */
    static void configurar(ComponentActivity activity, View raiz) {
        EdgeToEdge.enable(activity);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.statusBars());

        int left = raiz.getPaddingLeft();
        int top = raiz.getPaddingTop();
        int right = raiz.getPaddingRight();
        int bottom = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, windowInsets) -> {
            Insets i = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(left + i.left, top + i.top, right + i.right, bottom + i.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
