package com.example.mytraya2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class Intro_screen extends AppCompatActivity {

    private static final long DURACION_INTRO_MS = 5000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable abrirMenu = () -> {
        startActivity(new Intent(this, MenuScreen.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler.postDelayed(abrirMenu, DURACION_INTRO_MS);
    }

    @Override
    protected void onDestroy() {
        // Si el usuario sale durante la intro, no abrir el menú igualmente
        handler.removeCallbacks(abrirMenu);
        super.onDestroy();
    }
}
