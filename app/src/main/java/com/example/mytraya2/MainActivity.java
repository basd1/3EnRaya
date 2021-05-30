package com.example.mytraya2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView titulo, punt1, punt2, name1, name2; // titulo superior, puntuaciones y nombres
    ImageButton b11,b12,b13,b21,b22,b23,b31,b32,b33; // casillas del tablero
    Button b1, b2, b3; // botones inferiores
    boolean turno; // booleano para controlar el turno
    ArrayList<ImageButton> botones; // lista de las casillas del tablero
    Map<Integer, int[]> states_btns; // el int[0] significa el tag, el int[1] es la posicion
    ArrayList<int[]> check; // lista donde estan las combinaciones ganadoras
    ArrayList<Integer> jug1, jug2; // listas donde se almacenan las casillas de cada jugador
    int[] jug = {0,1,2}; // lista para controlar a quien pertenecen las casillas, 0=no usada, 1=j1, 2=j2

    // TODO un pequeño marcador para ver a quien le toca
    // TODO pdoer editar los nombres
    // TODO poder editar los colores
    // TODO habilitar la opcion de rendirse
    // TODO maquillar la app con mejoras de diseño y de animaciones, añadir sonidos?
    // TODO trabajar el online

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        turno = true;

        startMatch();
        checkturno(turno);

        b1.setOnClickListener(view -> Toast.makeText(MainActivity.this, "Has pulsado el botón izquierdo", Toast.LENGTH_SHORT).show());

        b2.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Has pulsado el botón central", Toast.LENGTH_SHORT).show();
            restartMatch();
            punt1.setText(String.valueOf(0));
            punt2.setText(String.valueOf(0));

        });

        b3.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Has pulsado el botón derecho", Toast.LENGTH_SHORT).show();
            restartMatch();
        });
    }

    private void startMatch() {

        states_btns = new HashMap<>();
        check = new ArrayList<>();
        jug1 = new ArrayList<>();
        jug2 = new ArrayList<>();

        declaraciones();
        seteandoClickListener();
        check = putSoluciones(check);

        botones = new ArrayList<>(Arrays.asList(b11,b12,b13,b21,b22,b23,b31,b32,b33));
        titulo.setText(R.string.prueba2);

        states_btns = putMapStates(botones, states_btns);

        name1.setText("VERDE");
        name2.setText("ROJO");
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
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_check)));
                aux2[1] = jug[1];
            }else{
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red_check)));
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
            titulo.setTextColor(getResources().getColor(R.color.green_check));
        }else{
            titulo.setTextColor(getResources().getColor(R.color.red_check));
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
                break;
            }else if (jug2.contains(lista[0]) && jug2.contains(lista[1]) && jug2.contains(lista[2])){
                Toast.makeText(this, "Gana el jugador 2", Toast.LENGTH_SHORT).show();
                Log.e("X","GANA EL J2");
                settingClickable(false);
                int p = Integer.parseInt(String.valueOf(punt2.getText()));
                p++;
                punt2.setText(String.valueOf(p));
                break;
            }else{
                Toast.makeText(this, "JUEGUE", Toast.LENGTH_SHORT).show();
            }
        }
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

        titulo = findViewById(R.id.text_titulo);

        punt1 = findViewById(R.id.punt1);
        punt2 = findViewById(R.id.punt2);

        name1 = findViewById(R.id.name1);
        name2 = findViewById(R.id.name2);

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

}