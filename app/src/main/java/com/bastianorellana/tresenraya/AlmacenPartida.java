package com.bastianorellana.tresenraya;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda entre sesiones los nombres, colores y marcador de cada modo de juego. */
interface AlmacenPartida {

    final class Datos {
        String titulo;
        String nombre1;
        String nombre2;
        int color1;
        int color2;
        int puntos1;
        int puntos2;

        Datos copia() {
            Datos d = new Datos();
            d.titulo = titulo;
            d.nombre1 = nombre1;
            d.nombre2 = nombre2;
            d.color1 = color1;
            d.color2 = color2;
            d.puntos1 = puntos1;
            d.puntos2 = puntos2;
            return d;
        }
    }

    Datos cargar(String clave, Datos porDefecto);

    void guardar(String clave, Datos datos);

    /** Implementación con SharedPreferences. */
    final class Preferencias implements AlmacenPartida {

        private final SharedPreferences prefs;

        Preferencias(Context context) {
            prefs = context.getApplicationContext().getSharedPreferences("partidas", Context.MODE_PRIVATE);
        }

        @Override
        public Datos cargar(String clave, Datos porDefecto) {
            Datos d = new Datos();
            d.titulo = prefs.getString(clave + ".titulo", porDefecto.titulo);
            d.nombre1 = prefs.getString(clave + ".nombre1", porDefecto.nombre1);
            d.nombre2 = prefs.getString(clave + ".nombre2", porDefecto.nombre2);
            d.color1 = prefs.getInt(clave + ".color1", porDefecto.color1);
            d.color2 = prefs.getInt(clave + ".color2", porDefecto.color2);
            d.puntos1 = prefs.getInt(clave + ".puntos1", porDefecto.puntos1);
            d.puntos2 = prefs.getInt(clave + ".puntos2", porDefecto.puntos2);
            return d;
        }

        @Override
        public void guardar(String clave, Datos d) {
            prefs.edit()
                    .putString(clave + ".titulo", d.titulo)
                    .putString(clave + ".nombre1", d.nombre1)
                    .putString(clave + ".nombre2", d.nombre2)
                    .putInt(clave + ".color1", d.color1)
                    .putInt(clave + ".color2", d.color2)
                    .putInt(clave + ".puntos1", d.puntos1)
                    .putInt(clave + ".puntos2", d.puntos2)
                    .apply();
        }
    }
}
