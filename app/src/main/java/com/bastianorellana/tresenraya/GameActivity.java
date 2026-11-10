package com.bastianorellana.tresenraya;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.SavedStateHandleSupport;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import java.util.Random;

import dev.sasikanth.colorsheet.ColorSheet;
import kotlin.Unit;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

/** Pantalla de juego: solo pinta el {@link EstadoPartida} y reenvía las acciones al ViewModel. */
public class GameActivity extends AppCompatActivity {

    private static final String EXTRA_DIFICULTAD = "dificultad";
    private static final int MAX_LONGITUD_NOMBRE = 13;
    private static final float DESPLAZAMIENTO_CELEBRACION_DP = 50f;
    private static final float ALFA_CASILLA_PERDEDORA = 0.45f;

    static Intent intentLocal(Context context) {
        return new Intent(context, GameActivity.class);
    }

    static Intent intentContraIa(Context context, Ia.Dificultad dificultad) {
        return new Intent(context, GameActivity.class).putExtra(EXTRA_DIFICULTAD, dificultad.name());
    }

    private GameViewModel viewModel;
    @Nullable private EstadoPartida estadoActual;
    private boolean primerPintado = true;

    private TextView titulo, punt1, punt2, name1, name2, mark1, mark2;
    private TextView[] textosGanador;
    private final ImageButton[] casillas = new ImageButton[Tablero.NUM_CASILLAS];
    private ContenedorCuadrado contenedorTablero;
    private LineaGanadoraView lineaGanadora;
    private FrameLayout celebracionLayout;
    private KonfettiView konfetti;
    private AnimationDrawable fondoAnimado;
    @Nullable private ObjectAnimator pulsoIa;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        View raiz = findViewById(R.id.layout_master);
        Pantalla.configurar(this, raiz);
        fondoAnimado = (AnimationDrawable) raiz.getBackground();
        fondoAnimado.setEnterFadeDuration(10);
        fondoAnimado.setExitFadeDuration(2000);

        enlazarVistas();
        viewModel = new ViewModelProvider(this, crearFactory()).get(GameViewModel.class);
        configurarListeners();

        if (savedInstanceState != null) cerrarColorSheetRestaurado();

        viewModel.getEstado().observe(this, this::pintar);
        viewModel.getEventos().observe(this, evento -> {
            if (evento.consumir()) reaccionar(evento);
        });

        // Si cambia el tamaño del tablero (p. ej. multiventana) recolocar la raya
        contenedorTablero.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (estadoActual != null && estadoActual.lineaGanadora != null) {
                v.post(() -> colocarLinea(estadoActual.lineaGanadora, false));
            }
        });
    }

    private ViewModelProvider.Factory crearFactory() {
        String extra = getIntent().getStringExtra(EXTRA_DIFICULTAD);
        Ia.Dificultad dificultad = extra == null ? null : Ia.Dificultad.valueOf(extra);

        AlmacenPartida.Datos porDefecto = new AlmacenPartida.Datos();
        porDefecto.titulo = getString(dificultad == null ? R.string.titulo_partida : R.string.titulo_partida_ia);
        porDefecto.nombre1 = getString(R.string.nombre_j1);
        porDefecto.nombre2 = getString(dificultad == null ? R.string.nombre_j2 : R.string.nombre_ia);
        porDefecto.color1 = ContextCompat.getColor(this, R.color.green_check);
        porDefecto.color2 = ContextCompat.getColor(this, R.color.red_check);

        AlmacenPartida almacen = new AlmacenPartida.Preferencias(this);

        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> clase, @NonNull CreationExtras extras) {
                SavedStateHandle estadoGuardado = SavedStateHandleSupport.createSavedStateHandle(extras);
                return (T) new GameViewModel(estadoGuardado, almacen, new Programador.HiloPrincipal(),
                        new Ia(new Random()), dificultad, porDefecto);
            }
        };
    }

    private void enlazarVistas() {
        celebracionLayout = findViewById(R.id.celebrate_layout);
        konfetti = findViewById(R.id.viewKonfetti);
        contenedorTablero = findViewById(R.id.tablero_contenedor);
        lineaGanadora = findViewById(R.id.linea_ganadora);

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
            casillas[i].setOnClickListener(v -> viewModel.pulsarCasilla(casilla));
        }

        findViewById(R.id.btn_izq).setOnClickListener(v -> finish());
        findViewById(R.id.btn_ctn).setOnClickListener(v -> {
            cancelarCelebracion();
            viewModel.reiniciarMarcador();
        });
        findViewById(R.id.btn_drc).setOnClickListener(v -> {
            cancelarCelebracion();
            viewModel.otraPartida();
        });

        name1.setOnClickListener(v -> mostrarCambioNombre(1));
        name2.setOnClickListener(v -> mostrarCambioNombre(2));
        titulo.setOnClickListener(v -> mostrarCambioNombre(3));

        punt1.setOnClickListener(v -> mostrarSelectorColor(1));
        mark1.setOnClickListener(v -> mostrarSelectorColor(1));
        punt2.setOnClickListener(v -> mostrarSelectorColor(2));
        mark2.setOnClickListener(v -> mostrarSelectorColor(2));
    }

    // ---------------------------------------------------------------- Pintado

    private void pintar(EstadoPartida e) {
        estadoActual = e;
        AlmacenPartida.Datos d = e.datos;

        titulo.setText(d.titulo);
        name1.setText(d.nombre1);
        name2.setText(d.nombre2);
        name1.setTextColor(d.color1);
        name2.setTextColor(d.color2);
        punt1.setText(String.valueOf(d.puntos1));
        punt2.setText(String.valueOf(d.puntos2));

        name1.setContentDescription(getString(R.string.nombre_descripcion, d.nombre1));
        name2.setContentDescription(getString(R.string.nombre_descripcion, d.nombre2));
        punt1.setContentDescription(getString(R.string.puntuacion_descripcion, d.nombre1, d.puntos1));
        punt2.setContentDescription(getString(R.string.puntuacion_descripcion, d.nombre2, d.puntos2));

        mark1.setBackgroundTintList(ColorStateList.valueOf(d.color1));
        mark2.setBackgroundTintList(ColorStateList.valueOf(d.color2));
        mark1.setVisibility(e.turnoJ1 ? View.VISIBLE : View.INVISIBLE);
        mark2.setVisibility(e.turnoJ1 ? View.INVISIBLE : View.VISIBLE);
        mark1.setContentDescription(getString(R.string.turno_de, d.nombre1));
        mark2.setContentDescription(e.iaPensando
                ? getString(R.string.ia_pensando)
                : getString(R.string.turno_de, d.nombre2));
        actualizarPulsoIa(e.iaPensando);

        int colorVacia = ContextCompat.getColor(this, R.color.blue_start);
        for (int i = 0; i < casillas.length; i++) {
            int estadoCasilla = e.casillas[i];
            ImageButton casilla = casillas[i];
            int color = estadoCasilla == Tablero.J1 ? d.color1
                    : estadoCasilla == Tablero.J2 ? d.color2 : colorVacia;
            casilla.setBackgroundTintList(ColorStateList.valueOf(color));
            casilla.setImageResource(estadoCasilla == Tablero.J1 ? R.drawable.ic_ficha_x
                    : estadoCasilla == Tablero.J2 ? R.drawable.ic_ficha_o : 0);
            casilla.setAlpha(e.lineaGanadora != null && !contiene(e.lineaGanadora, i)
                    ? ALFA_CASILLA_PERDEDORA : 1f);

            String contenido = estadoCasilla == Tablero.J1 ? d.nombre1
                    : estadoCasilla == Tablero.J2 ? d.nombre2 : getString(R.string.casilla_vacia);
            casilla.setContentDescription(getString(R.string.casilla_descripcion, i / 3 + 1, i % 3 + 1, contenido));
        }

        if (e.lineaGanadora == null) {
            lineaGanadora.ocultar();
        } else if (!lineaGanadora.visible()) {
            // Animada si aparece ahora; estática si venimos de girar la pantalla
            boolean animar = !primerPintado;
            int[] linea = e.lineaGanadora;
            contenedorTablero.post(() -> colocarLinea(linea, animar));
        }
        primerPintado = false;
    }

    private void colocarLinea(int[] linea, boolean animar) {
        float[] inicio = contenedorTablero.centroDe(casillas[linea[0]]);
        float[] fin = contenedorTablero.centroDe(casillas[linea[2]]);
        lineaGanadora.mostrar(inicio[0], inicio[1], fin[0], fin[1], animar);
    }

    private static boolean contiene(int[] valores, int valor) {
        for (int v : valores) if (v == valor) return true;
        return false;
    }

    private void actualizarPulsoIa(boolean pensando) {
        if (pensando && pulsoIa == null) {
            pulsoIa = ObjectAnimator.ofFloat(mark2, View.ALPHA, 1f, 0.25f);
            pulsoIa.setDuration(350);
            pulsoIa.setRepeatMode(ValueAnimator.REVERSE);
            pulsoIa.setRepeatCount(ValueAnimator.INFINITE);
            pulsoIa.start();
        } else if (!pensando && pulsoIa != null) {
            pulsoIa.cancel();
            pulsoIa = null;
            mark2.setAlpha(1f);
        }
    }

    // ---------------------------------------------------------------- Eventos

    private void reaccionar(Evento evento) {
        ImageButton casilla = casillas[evento.casilla];
        if (evento.tipo == Evento.Tipo.CASILLA_OCUPADA) {
            vibrar(casilla, Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    ? HapticFeedbackConstants.REJECT : HapticFeedbackConstants.LONG_PRESS);
            casilla.animate().cancel();
            casilla.setTranslationX(0f);
            ObjectAnimator.ofFloat(casilla, View.TRANSLATION_X, 0, 18, -18, 12, -12, 0)
                    .setDuration(300).start();
            Toast.makeText(this, R.string.casilla_ocupada, Toast.LENGTH_SHORT).show();
            return;
        }

        // Ficha nueva: aparece con un pequeño rebote
        casilla.setScaleX(0.6f);
        casilla.setScaleY(0.6f);
        casilla.animate().scaleX(1f).scaleY(1f).setDuration(250)
                .setInterpolator(new OvershootInterpolator(3f)).start();
        vibrar(casilla, HapticFeedbackConstants.VIRTUAL_KEY);

        if (estadoActual == null) return;
        switch (evento.resultado) {
            case GANA_J1:
                celebrar(estadoActual.datos.nombre1);
                break;
            case GANA_J2:
                celebrar(estadoActual.datos.nombre2);
                break;
            case EMPATE:
                Toast.makeText(this, R.string.empate, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private static void vibrar(View v, int tipo) {
        v.performHapticFeedback(tipo);
    }

    // ---------------------------------------------------------------- Diálogos

    private void mostrarCambioNombre(int objetivo) {
        if (estadoActual == null) return;
        AlmacenPartida.Datos d = estadoActual.datos;
        String actual = objetivo == 1 ? d.nombre1 : objetivo == 2 ? d.nombre2 : d.titulo;

        final EditText input = new EditText(this);
        input.setText(actual);
        input.setSelection(input.getText().length());
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LONGITUD_NOMBRE)});

        new AlertDialog.Builder(this)
                .setTitle(R.string.cambiar_nombre_titulo)
                .setMessage(R.string.cambiar_nombre_mensaje)
                .setView(input)
                .setPositiveButton(R.string.ok, (dialog, which) ->
                        viewModel.cambiarNombre(objetivo, input.getText().toString()))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void mostrarSelectorColor(int jugador) {
        if (estadoActual == null) return;
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
        Integer seleccionado = jugador == 1 ? estadoActual.datos.color1 : estadoActual.datos.color2;

        new ColorSheet().colorPicker(colores, seleccionado, false, color -> {
            viewModel.cambiarColor(jugador, color);
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
        vibrar(contenedorTablero, Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.LONG_PRESS);
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

        handler.removeCallbacksAndMessages(null);
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
        if (pulsoIa != null) pulsoIa.cancel();
        super.onDestroy();
    }
}
