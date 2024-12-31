package es.udc.psi.ttprototipo1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Disk {
    private float x, y, radius;
    private float vx = 5, vy = 5; // Velocidades iniciales
    private final float friction = 0.00f;
    // Limita la velocidad máxima
    float maxSpeed = 20f;



    public Disk(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    private float calculateAngle(float vx, float vy) {
        return (float) Math.toDegrees(Math.atan2(vy, vx));
    }

    public float getX(){
        return this.x;
    }

    public float getY(){
        return this.y;
    }

    public float getVx(){
        return this.vx;
    }

    public float getVY(){
        return this.vy;
    }

    public boolean update(long deltaTime, RectF playerPaddle, boolean isBottomPlayer, OnDiskExitListener listener) {
        // Actualiza la posición del disco
        x += vx * deltaTime / 16;
        y += vy * deltaTime / 16;

        // Rebote en los bordes laterales
        if (x - radius < 0 || x + radius > 1080) {
            vx = -vx;
        }

        // Detectar si el disco llega al techo
        if (y - radius < 0) {
            // Llamar al listener para notificar la salida
            listener.onDiskExit(x, y, calculateAngle(vx, vy), isBottomPlayer ? "bottom" : "top");
            return false; // Indicar que el disco ha salido
        }

        // Detectar colisión con la pala del jugador
        if (RectF.intersects(playerPaddle, new RectF(x - radius, y - radius, x + radius, y + radius))) {
            vy = -vy; // Rebote en el eje Y
            vx += (x - (playerPaddle.left + playerPaddle.right) / 2) * 0.05f; // Ajuste en X
        }

        return true; // Disco sigue en juego
    }




    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
    }

    // Método para actualizar la posición del disco
    public void setPosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    // Método para actualizar la velocidad del disco
    public void setVelocity(float newVx, float newVy) {
        this.vx = newVx;
        this.vy = newVy;
    }
}
