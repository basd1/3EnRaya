package com.bastianorellana.tresenraya;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/** Juega partidas reales sobre la interfaz. */
@RunWith(AndroidJUnit4.class)
public class GameActivityTest {

    private Context context;

    @Before
    public void borrarPartidasGuardadas() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("partidas", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void ganaJ1_sumaPuntoYCelebra() {
        try (ActivityScenario<GameActivity> ignored = ActivityScenario.launch(GameActivity.intentLocal(context))) {
            pulsar(R.id.btn_arriba_izq);  // J1
            pulsar(R.id.btn_centro_izq);  // J2
            pulsar(R.id.btn_arriba_ctr);  // J1
            pulsar(R.id.btn_centro_ctr);  // J2
            pulsar(R.id.btn_arriba_drc);  // J1 completa la fila de arriba

            onView(withId(R.id.punt1)).check(matches(withText("1")));
            onView(withId(R.id.punt2)).check(matches(withText("0")));
            onView(withId(R.id.win_blanco)).check(matches(withText(containsString("J1"))));
        }
    }

    @Test
    public void casillasDescritasParaLectoresDePantalla() {
        try (ActivityScenario<GameActivity> ignored = ActivityScenario.launch(GameActivity.intentLocal(context))) {
            pulsar(R.id.btn_centro_ctr);
            onView(withId(R.id.btn_centro_ctr))
                    .check(matches(withContentDescription(containsString("J1"))));
        }
    }

    @Test
    public void marcadorSeConservaAlVolverAEntrar() {
        try (ActivityScenario<GameActivity> ignored = ActivityScenario.launch(GameActivity.intentLocal(context))) {
            pulsar(R.id.btn_arriba_izq);
            pulsar(R.id.btn_centro_izq);
            pulsar(R.id.btn_arriba_ctr);
            pulsar(R.id.btn_centro_ctr);
            pulsar(R.id.btn_arriba_drc);
        }
        try (ActivityScenario<GameActivity> ignored = ActivityScenario.launch(GameActivity.intentLocal(context))) {
            onView(withId(R.id.punt1)).check(matches(withText("1")));
        }
    }

    private static void pulsar(int id) {
        onView(withId(id)).perform(click());
    }
}
