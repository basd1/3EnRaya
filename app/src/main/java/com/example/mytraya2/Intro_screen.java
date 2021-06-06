package com.example.mytraya2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class Intro_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("X","Pasa por la intro");
        Intent intent = new Intent(this, MainActivity.class);
        SystemClock.sleep(5000);
        startActivity(intent);
        finish();
    }
}