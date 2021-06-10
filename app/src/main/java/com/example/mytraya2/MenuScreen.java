package com.example.mytraya2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MenuScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        Button b1, b2, b3;

        b1 = findViewById(R.id.principal_btn1);
        b2 = findViewById(R.id.principal_btn2);
        b3 = findViewById(R.id.principal_btn3);

        b1.setText(getString(R.string.local));
        b2.setText(getString(R.string.online));
        b3.setText(getString(R.string.incognita));

        b1.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        b2.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            Toast.makeText(MenuScreen.this, "ONLINE AUN NO LISTO", Toast.LENGTH_SHORT).show();
        });

        b3.setOnClickListener(view -> Toast.makeText(MenuScreen.this, "Esta opción aún no está disponible", Toast.LENGTH_LONG).show());

    }
}