package com.example.mytraya2;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import dev.sasikanth.colorsheet.ColorSheet;
import kotlin.Unit;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_LONGITUD_NOMBRE = 13;
    private static final float DESPLAZAMIENTO_CELEBRACION_DP = 50f;

    private static final String KEY_TABLERO = "tablero";
    private static final String KEY_TURNO_J1 = "turno_j1";
    private static final String KEY_TERMINADA = "terminada";
    private static final String KEY_PUNT1 = "punt1";
    private static final String KEY_PUNT2 = "punt2";
    private static final String KEY_NOMBRE1 = "nombre1";
    private static final String KEY_NOMBRE2 = "nombre2";
    private static final String KEY_NOMBRE_PARTIDA = "nombre_partida";
    private static final String KEY_COLOR1 = "color1";
    private static final String KEY_COLOR2 = "color2";

    // Estado de la partida
    private final Tablero tablero = new Tablero();
    private boolean turnoJ1 = true;
    private boolean partidaTerminada = false;
    private int puntos1 = 0, puntos2 = 0;
    private String nombre1, nombre2, nombrePartida;
    private int colorJ1, colorJ2, colorVacia;

    // Vistas
    private TextView titulo, punt1, punt2, name1, name2, mark1, mark2;
    private TextView[] textosGanador;
    private final ImageButton[] casillas = new ImageButton[Tablero.NUM_CASILLAS];
    private FrameLayout celebracionLayout;
    private KonfettiView konfetti;
    private AnimationDrawable fondoAnimado;

    private final Handler handler = new Handler(Looper.getMainLooper());

    // TODO maquillar la app con mejoras de diseño y de animaciones, añadir sonidos?
    // TODO trabajar el online

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout raiz = findViewById(R.id.layout_master);
        Pantalla.configurar(this, raiz);

        fondoAnimado = (AnimationDrawable) raiz.getBackground();
        fondoAnimado.setEnterFadeDuration(10);
        fondoAnimado.setExitFadeDuration(2000);

        enlazarVistas();
        configurarListeners();

        colorVacia = ContextCompat.getColor(this, R.color.blue_start);
        if (savedInstanceState == null) {
            nombre1 = getString(R.string.nombre_j1);
            nombre2 = getString(R.string.nombre_j2);
            nombrePartida = getString(R.string.prueba);
            colorJ1 = ContextCompat.getColor(this, R.color.green_check);
            colorJ2 = ContextCompat.getColor(this, R.color.red_check);
        } else {
            restaurarEstado(savedInstanceState);
            cerrarColorSheetRestaurado();
        }

        refrescarUi();
    }

    private void enlazarVistas() {
        celebracionLayout = findViewById(R.id.celebrate_layout);
        konfetti = findViewById(R.id.viewKonfetti);

        titulo = findViewById(R.id.text_titulo);
        punt1 = findViewById(R.id.punt1);
        punt2 = findViewById(R.id.punt2);
        name1 = findViewById(R.id.name1);
        name2 = findViewById(R.id.name2);
        mark1 = findViewById(R.id.mark1);
        mark2 = findViewById(R.id.mark2);

        textosGanador = new TextView[]{
                findViewById(R.id.win_blanco),
                findViewById(R.id.win_yellow),
                findViewById(R.id.win_orange)};

        int[] ids = {
                R.id.btn_arriba_izq, R.id.btn_arriba_ctr, R.id.btn_arriba_drc,
                R.id.btn_centro_izq, R.id.btn_centro_ctr, R.id.btn_centro_drc,
                R.id.btn_abajo_izq, R.id.btn_abajo_ctr, R.id.btn_abajo_drc};
        for (int i = 0; i < ids.length; i++) {
            casillas[i] = findViewById(ids[i]);
        }
    }

    private void configurarListeners() {
        for (int i = 0; i < casillas.length; i++) {
            final int casilla = i;
            casillas[i].setOnClickListener(v -> jugar(casilla));
        }

        Button online = findViewById(R.id.btn_izq);
        Button restart = findViewById(R.id.btn_ctn);
        Button otra = findViewById(R.id.btn_drc);

        online.setText(R.string.online);
        online.setOnClickListener(v ->
                Toast.makeText(this, R.string.online_no_disponible, Toast.LENGTH_SHORT).show());

        // RESTART: partida nueva y marcador a cero
        restart.setOnClickListener(v -> {
            puntos1 = 0;
            puntos2 = 0;
            turnoJ1 = true;
            nuevaPartida();
        });

        // OTRA: partida nueva conservando el marcador
        otra.setOnClickListener(v -> nuevaPartida());

        name1.setOnClickListener(v -> mostrarCambioNombre(1));
        name2.setOnClickListener(v -> mostrarCambioNombre(2));
        titulo.setOnClickListener(v -> mostrarCambioNombre(3));

        punt1.setOnClickListener(v -> mostrarSelectorColor(1));
        mark1.setOnClickListener(v -> mostrarSelectorColor(1));
        punt2.setOnClickListener(v -> mostrarSelectorColor(2));
        mark2.setOnClickListener(v -> mostrarSelectorColor(2));
    }

    // ---------------------------------------------------------------- Lógica de juego

    private void jugar(int casilla) {
        if (partidaTerminada) return;

        int jugador = turnoJ1 ? Tablero.J1 : Tablero.J2;
        if (!tablero.jugar(casilla, jugador)) {
            Toast.makeText(this, R.string.casilla_ocupada, Toast.LENGTH_SHORT).show();
            return;
        }

        int ganador = tablero.ganador();
        if (ganador == Tablero.J1) {
            puntos1++;
            terminarPartida();
            celebrar(nombre1);
        } else if (ganador == Tablero.J2) {
            puntos2++;
            terminarPartida();
            celebrar(nombre2);
        } else if (tablero.lleno()) {
            terminarPartida();
            Toast.makeText(this, R.string.empate, Toast.LENGTH_SHORT).show();
        }

        // El turno alterna siempre, así la siguiente partida la empieza el otro jugador
        turnoJ1 = !turnoJ1;
        refrescarUi();
    }

    private void terminarPartida() {
        partidaTerminada = true;
    }

    private void nuevaPartida() {
        cancelarCelebracion();
        tablero.reiniciar();
        partidaTerminada = false;
        refrescarUi();
    }

    // ---------------------------------------------------------------- UI

    private void refrescarUi() {
        titulo.setText(nombrePartida);
        name1.setText(nombre1);
        name2.setText(nombre2);
        name1.setTextColor(colorJ1);
        name2.setTextColor(colorJ2);
        punt1.setText(String.valueOf(puntos1));
        punt2.setText(String.valueOf(puntos2));

        mark1.setBackgroundTintList(ColorStateList.valueOf(colorJ1));
        mark2.setBackgroundTintList(ColorStateList.valueOf(colorJ2));
        mark1.setVisibility(turnoJ1 ? View.VISIBLE : View.INVISIBLE);
        mark2.setVisibility(turnoJ1 ? View.INVISIBLE : View.VISIBLE);

        for (int i = 0; i < casillas.length; i++) {
            casillas[i].setBackgroundTintList(ColorStateList.valueOf(colorDeCasilla(tablero.get(i))));
        }
    }

    private int colorDeCasilla(int estado) {
        if (estado == Tablero.J1) return colorJ1;
        if (estado == Tablero.J2) return colorJ2;
        return colorVacia;
    }

    private void mostrarCambioNombre(int objetivo) {
        String actual = objetivo == 1 ? nombre1 : objetivo == 2 ? nombre2 : nombrePartida;

        final EditText input = new EditText(this);
        input.setText(actual);
        input.setSelection(input.getText().length());
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LONGITUD_NOMBRE)});

        new AlertDialog.Builder(this)
                .setTitle(R.string.cambiar_nombre_titulo)
                .setMessage(R.string.cambiar_nombre_mensaje)
                .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String nuevo = input.getText().toString().trim();
                    if (nuevo.isEmpty()) return; // un nombre vacío dejaría el texto invisible
                    if (objetivo == 1) nombre1 = nuevo;
                    else if (objetivo == 2) nombre2 = nuevo;
                    else nombrePartida = nuevo;
                    refrescarUi();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void mostrarSelectorColor(int jugador) {
        int[] colores = {
                ContextCompat.getColor(this, R.color.green_check),
                ContextCompat.getColor(this, R.color.red_check),
                ContextCompat.getColor(this, R.color.orange),
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.teal_700),
                ContextCompat.getColor(this, R.color.purple_200),
                ContextCompat.getColor(this, R.color.yellow),
                ContextCompat.getColor(this, R.color.azul_oscuro),
                ContextCompat.getColor(this, R.color.gris),
                ContextCompat.getColor(this, R.color.rosa),
                ContextCompat.getColor(this, R.color.verde_oscuro),
                Color.WHITE};
        Integer seleccionado = jugador == 1 ? colorJ1 : colorJ2;

        new ColorSheet().colorPicker(colores, seleccionado, false, color -> {
            if (jugador == 1) colorJ1 = color;
            else colorJ2 = color;
            refrescarUi();
            return Unit.INSTANCE;
        }).show(getSupportFragmentManager());
    }

    /**
     * ColorSheet no guarda sus colores ni su callback al recrearse la actividad (al girar
     * la pantalla, por ejemplo) y reaparecería vacío. Se cierra para que el usuario lo
     * vuelva a abrir.
     */
    private void cerrarColorSheetRestaurado() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof ColorSheet) {
                ((ColorSheet) f).dismissAllowingStateLoss();
            }
        }
    }

    // ---------------------------------------------------------------- Celebración

    private void celebrar(String ganador) {
        Toast.makeText(this, getString(R.string.gana_jugador, ganador), Toast.LENGTH_SHORT).show();

        konfetti.build()
                .addColors(Color.YELLOW, Color.WHITE)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, konfetti.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 5000L);

        String texto = getString(R.string.ganador, ganador);
        for (TextView tv : textosGanador) {
            tv.setText(texto);
            tv.setVisibility(View.VISIBLE);
        }
        TextView amarillo = textosGanador[1];
        TextView naranja = textosGanador[2];

        float desplazamiento = DESPLAZAMIENTO_CELEBRACION_DP * getResources().getDisplayMetrics().density;

        celebracionLayout.animate().cancel();
        celebracionLayout.setTranslationY(0f);
        celebracionLayout.setAlpha(0f);
        celebracionLayout.setVisibility(View.VISIBLE);
        celebracionLayout.animate().alpha(1f).translationY(-desplazamiento);

        // Parpadeo alternando los colores del texto
        handler.postDelayed(() -> naranja.setVisibility(View.INVISIBLE), 500);
        handler.postDelayed(() -> amarillo.setVisibility(View.INVISIBLE), 1000);
        handler.postDelayed(() -> naranja.setVisibility(View.VISIBLE), 1500);
        handler.postDelayed(() -> {
            amarillo.setVisibility(View.VISIBLE);
            naranja.setVisibility(View.INVISIBLE);
        }, 2000);
        handler.postDelayed(() -> amarillo.setVisibility(View.INVISIBLE), 2500);
        handler.postDelayed(() -> naranja.setVisibility(View.VISIBLE), 3000);
        handler.postDelayed(() -> celebracionLayout.animate()
                .alpha(0f)
                .translationY(desplazamiento)
                .withEndAction(this::ocultarCelebracion), 4500);
    }

    private void cancelarCelebracion() {
        handler.removeCallbacksAndMessages(null);
        celebracionLayout.animate().cancel();
        ocultarCelebracion();
    }

    private void ocultarCelebracion() {
        celebracionLayout.setVisibility(View.INVISIBLE);
        celebracionLayout.setAlpha(1f);
        celebracionLayout.setTranslationY(0f);
    }

    // ---------------------------------------------------------------- Ciclo de vida

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(KEY_TABLERO, tablero.exportar());
        outState.putBoolean(KEY_TURNO_J1, turnoJ1);
        outState.putBoolean(KEY_TERMINADA, partidaTerminada);
        outState.putInt(KEY_PUNT1, puntos1);
        outState.putInt(KEY_PUNT2, puntos2);
        outState.putString(KEY_NOMBRE1, nombre1);
        outState.putString(KEY_NOMBRE2, nombre2);
        outState.putString(KEY_NOMBRE_PARTIDA, nombrePartida);
        outState.putInt(KEY_COLOR1, colorJ1);
        outState.putInt(KEY_COLOR2, colorJ2);
    }

    private void restaurarEstado(@NonNull Bundle in) {
        tablero.importar(in.getIntArray(KEY_TABLERO));
        turnoJ1 = in.getBoolean(KEY_TURNO_J1, true);
        partidaTerminada = in.getBoolean(KEY_TERMINADA, false);
        puntos1 = in.getInt(KEY_PUNT1);
        puntos2 = in.getInt(KEY_PUNT2);
        nombre1 = in.getString(KEY_NOMBRE1, getString(R.string.nombre_j1));
        nombre2 = in.getString(KEY_NOMBRE2, getString(R.string.nombre_j2));
        nombrePartida = in.getString(KEY_NOMBRE_PARTIDA, getString(R.string.prueba));
        colorJ1 = in.getInt(KEY_COLOR1, ContextCompat.getColor(this, R.color.green_check));
        colorJ2 = in.getInt(KEY_COLOR2, ContextCompat.getColor(this, R.color.red_check));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!fondoAnimado.isRunning()) fondoAnimado.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fondoAnimado.isRunning()) fondoAnimado.stop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
