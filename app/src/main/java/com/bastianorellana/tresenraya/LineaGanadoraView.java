package com.bastianorellana.tresenraya;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

/** Dibuja, con una animación, la raya que tacha las tres casillas ganadoras. */
public class LineaGanadoraView extends View {

    private static final long DURACION_MS = 450;

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    @Nullable private float[] linea; // x0, y0, x1, y1
    private float progreso;
    @Nullable private ValueAnimator animador;

    public LineaGanadoraView(Context context) {
        this(context, null);
    }

    public LineaGanadoraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float densidad = getResources().getDisplayMetrics().density;
        pincel.setColor(Color.WHITE);
        pincel.setStrokeWidth(10 * densidad);
        pincel.setStrokeCap(Paint.Cap.ROUND);
        pincel.setShadowLayer(6 * densidad, 3 * densidad, 3 * densidad, 0x99000000);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // necesario para la sombra en API < 28
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /**
     * Muestra la raya entre dos puntos (en coordenadas de esta vista). Se alarga un poco
     * más allá de los centros para que cruce las casillas de los extremos.
     */
    void mostrar(float x0, float y0, float x1, float y1, boolean animar) {
        float dx = (x1 - x0) * 0.18f;
        float dy = (y1 - y0) * 0.18f;
        linea = new float[]{x0 - dx, y0 - dy, x1 + dx, y1 + dy};
        cancelarAnimacion();
        if (animar) {
            animador = ValueAnimator.ofFloat(0f, 1f);
            animador.setDuration(DURACION_MS);
            animador.setInterpolator(new DecelerateInterpolator());
            animador.addUpdateListener(a -> {
                progreso = (float) a.getAnimatedValue();
                invalidate();
            });
            animador.start();
        } else {
            progreso = 1f;
            invalidate();
        }
    }

    void ocultar() {
        cancelarAnimacion();
        linea = null;
        invalidate();
    }

    boolean visible() {
        return linea != null;
    }

    private void cancelarAnimacion() {
        if (animador != null) {
            animador.cancel();
            animador = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (linea == null) return;
        float x = linea[0] + (linea[2] - linea[0]) * progreso;
        float y = linea[1] + (linea[3] - linea[1]) * progreso;
        canvas.drawLine(linea[0], linea[1], x, y, pincel);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelarAnimacion();
        super.onDetachedFromWindow();
    }
}
