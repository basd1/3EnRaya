package com.bastianorellana.tresenraya;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * FrameLayout que siempre es cuadrado: usa el lado más corto del espacio disponible.
 * Así el tablero se ve bien en móvil, tablet, vertical y horizontal.
 */
public class ContenedorCuadrado extends FrameLayout {

    public ContenedorCuadrado(Context context) {
        super(context);
    }

    public ContenedorCuadrado(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContenedorCuadrado(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int ancho = MeasureSpec.getSize(widthMeasureSpec);
        int alto = MeasureSpec.getSize(heightMeasureSpec);
        int lado;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) lado = ancho;
        else if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) lado = alto;
        else lado = Math.min(ancho, alto);
        int spec = MeasureSpec.makeMeasureSpec(lado, MeasureSpec.EXACTLY);
        super.onMeasure(spec, spec);
    }

    /** Centro de una vista descendiente, en coordenadas de este contenedor. */
    float[] centroDe(View hijo) {
        int[] yo = new int[2];
        int[] el = new int[2];
        getLocationInWindow(yo);
        hijo.getLocationInWindow(el);
        return new float[]{
                el[0] - yo[0] + hijo.getWidth() / 2f,
                el[1] - yo[1] + hijo.getHeight() / 2f};
    }
}
