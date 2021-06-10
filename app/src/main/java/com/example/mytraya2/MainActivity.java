package com.example.mytraya2;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import dev.sasikanth.colorsheet.ColorSheet;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;
import top.defaults.colorpicker.ColorPickerPopup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String nombre1, nombre2, nombrepartida, win;
    TextView titulo, punt1, punt2, name1, name2, mark1, mark2, win1, win2, win3; // titulo superior, puntuaciones y nombres
    ImageButton b11,b12,b13,b21,b22,b23,b31,b32,b33; // casillas del tablero
    Button b1, b2, b3; // botones inferiores
    boolean turno; // booleano para controlar el turno
    ArrayList<ImageButton> botones; // lista de las casillas del tablero
    Map<Integer, int[]> states_btns; // el int[0] significa el tag, el int[1] es la posicion
    ArrayList<int[]> check; // lista donde estan las combinaciones ganadoras
    ArrayList<Integer> jug1, jug2; // listas donde se almacenan las casillas de cada jugador
    int[] jug = {0,1,2}; // lista para controlar a quien pertenecen las casillas, 0=no usada, 1=j1, 2=j2
    int rojo, azul, verde, color_j1, color_j2;
    RelativeLayout lly;
    FrameLayout cel_ly;
    AnimationDrawable ad;

    // TODO maquillar la app con mejoras de diseño y de animaciones, añadir sonidos?
    // TODO trabajar el online

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        turno = true;

        declaraciones();
        preMatch();
        startMatch();
        checkturno(turno);

        b1.setOnClickListener(view -> Toast.makeText(MainActivity.this, "Has pulsado el botón izquierdo", Toast.LENGTH_SHORT).show());

        b2.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Has pulsado el botón central", Toast.LENGTH_SHORT).show();
            restartMatch();
            punt1.setText(String.valueOf(0));
            punt2.setText(String.valueOf(0));

        });

        b3.setOnClickListener(view -> restartMatch());

        name1.setOnClickListener(view1 -> showChangename(view1, 1));

        name2.setOnClickListener(view -> showChangename(view, 2));

        titulo.setOnClickListener(view -> showChangename(view, 3));

        punt1.setOnClickListener(view -> showColorPicker(1));
        mark1.setOnClickListener(view -> showColorPicker(1));

        punt2.setOnClickListener(view -> showColorPicker(2));
        mark2.setOnClickListener(view -> showColorPicker(2));
    }

    private void showColorPicker(int i) {

        int[] colors = {getResources().getColor(R.color.green_check),
        getResources().getColor(R.color.red_check),
        getResources().getColor(R.color.orange),
        getResources().getColor(R.color.black),
        getResources().getColor(R.color.teal_700),
        getResources().getColor(R.color.purple_200),
        getResources().getColor(R.color.yellow),
        getResources().getColor(R.color.azul_oscuro),
        getResources().getColor(R.color.gris),
        getResources().getColor(R.color.rosa),
        getResources().getColor(R.color.verde_oscuro),
        Color.WHITE};

        new ColorSheet().colorPicker(colors, null, false, new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer color) {
                if (i==1){
                    color_j1 = color;
                    mark1.setBackgroundTintList(ColorStateList.valueOf(color_j1));
                    name1.setTextColor(color_j1);

                }else if (i==2){
                    color_j2 = color;
                    mark2.setBackgroundTintList(ColorStateList.valueOf(color_j2));
                    name2.setTextColor(color_j2);
                }
                for (ImageButton ib2 : botones){
                    for (Integer key : states_btns.keySet()){
                        Log.e("X","La key de esto es "+key);
                        Log.e("X","El tag de esto es "+ib2.getTag());
                        if (ib2.getTag() == key){
                            int[] aux4 = states_btns.get(key);
                            if (aux4[1] == 1){
                                ib2.setBackgroundTintList(ColorStateList.valueOf(color_j1));
                            }else if (aux4[1] == 2){
                                ib2.setBackgroundTintList(ColorStateList.valueOf(color_j2));
                            }
                        }
                    }
                }
                return null;
            }
        }).show(getSupportFragmentManager());

    }

    private void preMatch() {
        name1.setText(nombre1);
        name2.setText(nombre2);

        name1.setTextColor(color_j1);
        name2.setTextColor(color_j2);

        titulo.setText(nombrepartida);

        b1.setText(R.string.online);

        mark1.setBackgroundTintList(ColorStateList.valueOf(color_j1));
        mark2.setBackgroundTintList(ColorStateList.valueOf(color_j2));

        mark1.setVisibility(View.INVISIBLE);
        mark2.setVisibility(View.INVISIBLE);
    }

    private void showChangename(View view, int i2) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        TextView tv = (TextView) view;
        alert.setTitle("Cambiar el nombre");
        alert.setMessage("Pon el nombre que quieras usar ahora");

        // Set an EditText view to get user input
        final EditText input = new EditText(MainActivity.this);
        input.setText(String.valueOf(tv.getText()));
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(13)});
        alert.setView(input);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            String newText = input.getText().toString();
            tv.setText(newText);
            if (i2==1){
                nombre1 = newText;
            }else if (i2 == 2){
                nombre2 = newText;
            }else if (i2 == 3){
                nombrepartida = newText;
            }
        });

        alert.create().show();
    }

    private void startMatch() {

        states_btns = new HashMap<>();
        check = new ArrayList<>();
        jug1 = new ArrayList<>();
        jug2 = new ArrayList<>();

        seteandoClickListener();
        check = putSoluciones(check);

        botones = new ArrayList<>(Arrays.asList(b11,b12,b13,b21,b22,b23,b31,b32,b33));

        states_btns = putMapStates(botones, states_btns);

    }

    private void restartMatch() {
        for (ImageButton boton : botones){
            boton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue_start)));
        }
        settingClickable(true);
        startMatch();
    }

    private Map<Integer, int[]> putMapStates(ArrayList<ImageButton> b, Map<Integer, int[]> sb) {

        int i = 0;
        Log.e("X","prueba");
        System.out.println(sb.size()+" "+ b.size());

        for (ImageButton ib: b){
            int[] auxxx = new int[2];
            auxxx[0] = i;
            auxxx[1] = jug[0];
            ib.setTag((i+5)*110697);
            sb.put((Integer) ib.getTag(),auxxx);
            i++;
        }
        return sb;
    }

    @Override
    public void onClick(View v) {

        int[] aux2 =  states_btns.get(v.getTag());

        if (aux2[1] == jug[0]){
            if (turno){
                v.setBackgroundTintList(ColorStateList.valueOf(color_j1));
                aux2[1] = jug[1];
            }else{
                v.setBackgroundTintList(ColorStateList.valueOf(color_j2));
                aux2[1] = jug[2];
            }
            states_btns.put((Integer) v.getTag(),aux2);
            updateStates();
            checkerWinner();
            turno = !turno;
            checkturno(turno);

        }else {
            Toast.makeText(this, "Juega en otra casilla", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkturno(boolean t) {

        if (t){
            mark1.setVisibility(View.VISIBLE);
            mark2.setVisibility(View.INVISIBLE);
        }else{
            mark1.setVisibility(View.INVISIBLE);
            mark2.setVisibility(View.VISIBLE);
        }
    }

    private void updateStates() {
        for (Integer key : states_btns.keySet()) {
            int[] aux3 = states_btns.get(key);
            if (aux3[1] == 0){
                System.out.println("En la posicion "+key+" "+aux3[0]+" nadie esta aun");
            }else if (aux3[1] == 1){
                System.out.println("En la posicion "+key+" "+aux3[0]+" esta el j1");
                jug1.add(aux3[0]);
            }else if (aux3[1] == 2) {
                System.out.println("En la posicion " + key + " " + aux3[0] + " esta el j2");
                jug2.add(aux3[0]);
            }else {
                Log.e("X", "algo falla");
            }
        }
    }

    private void checkerWinner() {

        for (int[] lista : check){

            System.out.println("la lista comprobando es " + lista[0] + " " + lista[1] + " " + lista[2]);

            if (jug1.contains(lista[0]) && jug1.contains(lista[1]) && jug1.contains(lista[2])){
                Toast.makeText(this, "Gana el jugador 1", Toast.LENGTH_SHORT).show();
                Log.e("X","GANA EL J1");
                settingClickable(false);
                int p = Integer.parseInt(String.valueOf(punt1.getText()));
                p++;
                punt1.setText(String.valueOf(p));
                celebracion(1);
                break;
            }else if (jug2.contains(lista[0]) && jug2.contains(lista[1]) && jug2.contains(lista[2])){
                Toast.makeText(this, "Gana el jugador 2", Toast.LENGTH_SHORT).show();
                Log.e("X","GANA EL J2");
                settingClickable(false);
                int p = Integer.parseInt(String.valueOf(punt2.getText()));
                p++;
                punt2.setText(String.valueOf(p));
                celebracion(2);

                break;
            }
        }
    }

    private void celebracion(int j) {

        if (j==1){
            win = nombre1;
        }else if (j==2){
            win = nombre2;
        }

        final KonfettiView viewKonfetti = findViewById(R.id.viewKonfetti);
        viewKonfetti.build()
                .addColors(Color.YELLOW, Color.WHITE)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5))
                .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 5000L);


        TextView[] tva = {win1, win2, win3};
        for (TextView tva0 : tva){
            tva0.setText("WINNER\n"+win);
        }
        //TODO corregir la celebracion, no se ve el mensaje en el textview
        cel_ly.setVisibility(View.VISIBLE);
        cel_ly.setAlpha(0.0f);
        cel_ly.animate().alpha(1.0f);
        cel_ly.animate().translationY(-150);

        Handler h = new Handler();
        h.postDelayed(() -> win3.setVisibility(View.INVISIBLE),500);
        h.postDelayed(() -> win2.setVisibility(View.INVISIBLE), 1000);
        h.postDelayed(() -> win3.setVisibility(View.VISIBLE), 1500);
        h.postDelayed(() -> {
            win2.setVisibility(View.VISIBLE);
            win3.setVisibility(View.INVISIBLE);
        },2000);
        h.postDelayed(() -> win2.setVisibility(View.INVISIBLE), 2500);
        h.postDelayed(() -> win3.setVisibility(View.VISIBLE), 3000);
        h.postDelayed(() -> {
            cel_ly.animate().translationY(150);
            cel_ly.animate().alpha(0.0f);
        },4500);
    }

    private void settingClickable(boolean b) {
        for (ImageButton ib : botones) ib.setClickable(b);
    }

    private void seteandoClickListener() {

        b11.setOnClickListener(this);
        b12.setOnClickListener(this);
        b13.setOnClickListener(this);
        b21.setOnClickListener(this);
        b22.setOnClickListener(this);
        b23.setOnClickListener(this);
        b31.setOnClickListener(this);
        b32.setOnClickListener(this);
        b33.setOnClickListener(this);
    }

    private void declaraciones() {

        lly = findViewById(R.id.layout_master);
        cel_ly = findViewById(R.id.celebrate_layout);

        ad = (AnimationDrawable) lly.getBackground();
        ad.setEnterFadeDuration(10);
        ad.setExitFadeDuration(2000);
        ad.start();

        titulo = findViewById(R.id.text_titulo);

        punt1 = findViewById(R.id.punt1);
        punt2 = findViewById(R.id.punt2);

        name1 = findViewById(R.id.name1);
        name2 = findViewById(R.id.name2);

        win1 = findViewById(R.id.win_blanco);
        win2 = findViewById(R.id.win_yellow);
        win3 = findViewById(R.id.win_orange);

        win = "";

        nombre1 = "J1";
        nombre2 = "J2";

        nombrepartida = getString(R.string.prueba);

        mark1 = findViewById(R.id.mark1);
        mark2 = findViewById(R.id.mark2);

        azul = getResources().getColor(R.color.blue_start);
        rojo = getResources().getColor(R.color.red_check);
        verde = getResources().getColor(R.color.green_check);

        color_j1 = verde;
        color_j2 = rojo;

        b11 = findViewById(R.id.btn_arriba_izq);
        b12 = findViewById(R.id.btn_arriba_ctr);
        b13 = findViewById(R.id.btn_arriba_drc);
        b21 = findViewById(R.id.btn_centro_izq);
        b22 = findViewById(R.id.btn_centro_ctr);
        b23 = findViewById(R.id.btn_centro_drc);
        b31 = findViewById(R.id.btn_abajo_izq);
        b32 = findViewById(R.id.btn_abajo_ctr);
        b33 = findViewById(R.id.btn_abajo_drc);

        b1 = findViewById(R.id.btn_izq);
        b2 = findViewById(R.id.btn_ctn);
        b3 = findViewById(R.id.btn_drc);
    }

    private ArrayList<int[]> putSoluciones(ArrayList<int[]> ch) {
        ch.add(new int[]{0, 1, 2});
        ch.add(new int[]{0, 3, 6});
        ch.add(new int[]{0, 4, 8});
        ch.add(new int[]{1, 4, 7});
        ch.add(new int[]{2, 5, 8});
        ch.add(new int[]{2, 4, 6});
        ch.add(new int[]{3, 4, 5});
        ch.add(new int[]{6, 7, 8});

        Log.e("X","hay un total de "+ch.size()+" soluciones");
        return ch;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ad!=null) {
            if (!ad.isRunning()) {
                ad.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(ad!=null) {
            super.onPause();
            if (ad.isRunning()) {
                ad.stop();
            }
        }
    }
}