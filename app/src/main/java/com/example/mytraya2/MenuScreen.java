package com.example.mytraya2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);
        Pantalla.configurar(this, findViewById(R.id.menu_root));

        Button local = findViewById(R.id.principal_btn1);
        Button online = findViewById(R.id.principal_btn2);
        Button incognita = findViewById(R.id.principal_btn3);

        local.setText(R.string.local);
        online.setText(R.string.online);
        incognita.setText(R.string.incognita);

        local.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));

        online.setOnClickListener(view ->
                Toast.makeText(this, R.string.online_no_disponible, Toast.LENGTH_SHORT).show());

        incognita.setOnClickListener(view ->
                Toast.makeText(this, R.string.opcion_no_disponible, Toast.LENGTH_LONG).show());
    }
}
