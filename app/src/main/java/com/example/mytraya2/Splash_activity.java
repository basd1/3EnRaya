package com.example.mytraya2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class Splash_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemClock.sleep(4000);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}