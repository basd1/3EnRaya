package com.bastianorellana.tresenraya;

import android.os.Handler;
import android.os.Looper;

/** Ejecuta tareas con retraso. Abstraído para poder testear el ViewModel sin Looper. */
interface Programador {

    void programar(Runnable tarea, long retrasoMs);

    void cancelarTodo();

    /** Implementación real sobre el hilo principal. */
    final class HiloPrincipal implements Programador {

        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void programar(Runnable tarea, long retrasoMs) {
            handler.postDelayed(tarea, retrasoMs);
        }

        @Override
        public void cancelarTodo() {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
