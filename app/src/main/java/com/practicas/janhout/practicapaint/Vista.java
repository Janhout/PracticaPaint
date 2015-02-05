package com.practicas.janhout.practicapaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class Vista extends View {

    private Paint pincel;
    private Coordenadas coordenadas;
    public int goma;
    public int colorActual;

    private Path p;
    private Canvas fondo;
    private Bitmap bm;

    private ArrayMaximo<Bitmap> deshacer;

    public static enum Dibujo {
        CUADRADO,
        CUADRADO_RELLENO,
        CIRCULO,
        CIRCULO_RELLENO,
        NORMAL,
        RECTA,
        OVAL,
        OVAL_RELLENO,
        GOMA
    }

    private Dibujo tipo;

    /*****************************************************************************/
    /***CONSTRUCTOR***************************************************************/
    /**
     * *************************************************************************
     */

    public Vista(Context context) {
        super(context);
        p = new Path();
        pincel = new Paint();
        pincel.setAntiAlias(true);
        pincel.setStrokeCap(Paint.Cap.ROUND);
        coordenadas = new Coordenadas();
        colorActual = Color.BLACK;
        goma = Color.WHITE;
        tipo = Dibujo.NORMAL;
        deshacer = new ArrayMaximo<>(5);
    }

    /*****************************************************************************/
    /***METODOS CLASE*************************************************************/
    /**
     * *************************************************************************
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tipo == Dibujo.CUADRADO_RELLENO || tipo == Dibujo.CIRCULO_RELLENO
                || tipo == Dibujo.OVAL_RELLENO) {
            pincel.setStyle(Paint.Style.FILL);
        } else if (tipo == Dibujo.CUADRADO || tipo == Dibujo.NORMAL ||
                tipo == Dibujo.CIRCULO || tipo == Dibujo.RECTA || tipo == Dibujo.OVAL
                || tipo == Dibujo.GOMA) {
            pincel.setStyle(Paint.Style.STROKE);
        }
        if (tipo == Dibujo.GOMA) {
            pincel.setColor(goma);
        } else {
            pincel.setColor(colorActual);
        }
        canvas.drawBitmap(bm, 0, 0, null);
        if (tipo == Dibujo.NORMAL || tipo == Dibujo.GOMA) {
            canvas.drawPath(p, pincel);
        } else if (tipo == Dibujo.CUADRADO || tipo == Dibujo.CUADRADO_RELLENO) {
            canvas.drawRect(Math.min(coordenadas.x0, coordenadas.xi),
                    Math.min(coordenadas.y0, coordenadas.yi),
                    Math.max(coordenadas.x0, coordenadas.xi),
                    Math.max(coordenadas.y0, coordenadas.yi), pincel);
        } else if (tipo == Dibujo.CIRCULO || tipo == Dibujo.CIRCULO_RELLENO) {
            canvas.drawCircle(coordenadas.x0, coordenadas.y0, coordenadas.radio, pincel);
        } else if (tipo == Dibujo.RECTA) {
            canvas.drawLine(coordenadas.x0, coordenadas.y0, coordenadas.xi, coordenadas.yi, pincel);
        } else if (tipo == Dibujo.OVAL || tipo == Dibujo.OVAL_RELLENO) {
            canvas.drawOval(new RectF(Math.min(coordenadas.x0, coordenadas.xi),
                    Math.min(coordenadas.y0, coordenadas.yi),
                    Math.max(coordenadas.x0, coordenadas.xi),
                    Math.max(coordenadas.y0, coordenadas.yi)), pincel);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                coordenadas.x0 = coordenadas.xi = x;
                coordenadas.y0 = coordenadas.yi = y;
                p.reset();
                p.moveTo(coordenadas.x0, coordenadas.y0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (tipo == Dibujo.NORMAL || tipo == Dibujo.GOMA) {
                    p.quadTo(coordenadas.xi, coordenadas.yi, (x + coordenadas.xi) / 2, (y + coordenadas.yi) / 2);
                } else if (tipo == Dibujo.CIRCULO || tipo == Dibujo.CIRCULO_RELLENO) {
                    coordenadas.radio = (float) Math.sqrt((Math.pow(coordenadas.x0
                            - coordenadas.xi, 2)) + (Math.pow(coordenadas.y0 - coordenadas.yi, 2)));
                }
                coordenadas.xi = x;
                coordenadas.yi = y;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                deshacer.add(bm.copy(bm.getConfig(), true));

                coordenadas.xi = x;
                coordenadas.yi = y;
                if(coordenadas.x0 == coordenadas.xi && coordenadas.y0 == coordenadas.yi) {
                    fondo.drawPoint(coordenadas.xi, coordenadas.yi, pincel);
                } else if (tipo == Dibujo.NORMAL || tipo == Dibujo.GOMA) {
                    fondo.drawPath(p, pincel);
                    p.reset();
                } else if (tipo == Dibujo.CUADRADO || tipo == Dibujo.CUADRADO_RELLENO) {
                    fondo.drawRect(Math.min(coordenadas.x0, coordenadas.xi),
                            Math.min(coordenadas.y0, coordenadas.yi),
                            Math.max(coordenadas.x0, coordenadas.xi),
                            Math.max(coordenadas.y0, coordenadas.yi), pincel);
                } else if (tipo == Dibujo.CIRCULO || tipo == Dibujo.CIRCULO_RELLENO) {
                    coordenadas.radio = (float) Math.sqrt((Math.pow(coordenadas.x0
                            - coordenadas.xi, 2)) + (Math.pow(coordenadas.y0 - coordenadas.yi, 2)));
                    fondo.drawCircle(coordenadas.x0, coordenadas.y0, coordenadas.radio, pincel);
                } else if (tipo == Dibujo.RECTA) {
                    fondo.drawLine(coordenadas.x0, coordenadas.y0, coordenadas.xi, coordenadas.yi, pincel);
                } else if (tipo == Dibujo.OVAL || tipo == Dibujo.OVAL_RELLENO) {
                    fondo.drawOval(new RectF(Math.min(coordenadas.x0, coordenadas.xi),
                            Math.min(coordenadas.y0, coordenadas.yi),
                            Math.max(coordenadas.x0, coordenadas.xi),
                            Math.max(coordenadas.y0, coordenadas.yi)), pincel);
                }
                invalidate();
                coordenadas.reset();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bm.eraseColor(Color.WHITE);
        fondo = new Canvas(bm);
    }

    /*****************************************************************************/
    /***GETTERS Y SETTERS*********************************************************/
    /**
     * *************************************************************************
     */

    public Paint getPincel() {
        return pincel;
    }

    public void setPincel(Paint pincel) {
        this.pincel = pincel;
    }

    public void setBitmap(Bitmap bm) {
        this.bm = bm;
    }

    public Bitmap getBitmap() {
        return bm;
    }

    public void setFondo(Canvas fondo) {
        this.fondo = fondo;
    }

    public void setTipo(Dibujo tipo) {
        this.tipo = tipo;
    }

    public void setGoma(int goma) {
        this.goma = goma;
    }

    public void setColorActual(int colorActual) {
        this.colorActual = colorActual;
    }

    public int getColorActual() {
        return colorActual;
    }

    public ArrayMaximo<Bitmap> getDeshacer(){
        return deshacer;
    }

    public void setDeshacer(ArrayMaximo<Bitmap> deshacer){
        this.deshacer = deshacer;
    }

    /*****************************************************************************/
    /***CLASE COORDENADAS*********************************************************/
    /*****************************************************************************/

    class Coordenadas{
        public float x0, y0, xi, yi, radio;

        public Coordenadas(){
            this(-1, -2, -3, -4, -5);
        }

        public Coordenadas(float x0, float y0, float xi, float yi, float radio) {
            this.x0 = x0;
            this.y0 = y0;
            this.xi = xi;
            this.yi = yi;
            this.radio = radio;
        }

        public void reset(){
            this.x0 = -1;
            this.y0 = -2;
            this.xi = -3;
            this.yi = -4;
            this.radio = -5;
        }
    }
}