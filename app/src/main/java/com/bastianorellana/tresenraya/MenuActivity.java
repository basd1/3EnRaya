package com.bastianorellana.tresenraya;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class MenuActivity extends AppCompatActivity {

    /** Tiempo mínimo que se ve el logo al abrir la app en frío. */
    private static final long DURACION_MINIMA_SPLASH_MS = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        if (savedInstanceState == null) {
            long fin = SystemClock.uptimeMillis() + DURACION_MINIMA_SPLASH_MS;
            splash.setKeepOnScreenCondition(() -> SystemClock.uptimeMillis() < fin);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Pantalla.configurar(this, findViewById(R.id.menu_root));

        findViewById(R.id.btn_local).setOnClickListener(v ->
                startActivity(GameActivity.intentLocal(this)));

        findViewById(R.id.btn_ia).setOnClickListener(v -> elegirDificultad());

        findViewById(R.id.btn_online).setOnClickListener(v ->
                Toast.makeText(this, R.string.online_no_disponible, Toast.LENGTH_SHORT).show());
    }

    private void elegirDificultad() {
        Ia.Dificultad[] dificultades = Ia.Dificultad.values();
        String[] nombres = {
                getString(R.string.dificultad_facil),
                getString(R.string.dificultad_media),
                getString(R.string.dificultad_dificil)};

        new AlertDialog.Builder(this)
                .setTitle(R.string.elegir_dificultad)
                .setItems(nombres, (dialog, which) ->
                        startActivity(GameActivity.intentContraIa(this, dificultades[which])))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }
}
