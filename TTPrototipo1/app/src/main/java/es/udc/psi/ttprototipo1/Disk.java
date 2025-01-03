package es.udc.psi.ttprototipo1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Disk {
    private float x, y, radius;
    private float vx = 10, vy = 10; // Velocidades iniciales
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

    public boolean update(long deltaTime, float height, float width, RectF playerPaddle, boolean isBottomPlayer, OnDiskExitListener listener) {
        // Actualiza la posición del disco
        x += vx * deltaTime / 16;
        y += vy * deltaTime / 16;

        // Incremento de velocidad tras cada rebote
        final float speedIncrementFactor = 1.05f; // Incrementar un 5% la velocidad tras cada rebote

        // Rebote en los bordes laterales
        if (x - radius < 0 || x + radius > width) {
            vx = -vx; // Rebote en X
            // Aumentar velocidad tras el rebote
            vx *= speedIncrementFactor;
            vy *= speedIncrementFactor;
        }

        // Detectar si el disco llega al techo
        if (y - radius < 0) {
            // Llamar al listener para notificar la salida
            listener.onDiskExit(x, y, calculateAngle(vx, vy), isBottomPlayer ? "bottom" : "top");
            return false;
        }

        // Detectar si el disco llega al borde inferior
        if (y + radius > height) {
            // Llamar al listener para notificar la salida por el borde inferior
            listener.onDiskExit(x, y, calculateAngle(vx, vy), isBottomPlayer ? "bottom" : "top");
        }

        // Detectar colisión con la pala del jugador
        if (RectF.intersects(playerPaddle, new RectF(x - radius, y - radius, x + radius, y + radius))) {
            vy = -vy; // Invertir dirección en el eje Y

            // Ajustar la velocidad en el eje X según el punto de impacto en la pala
            float paddleCenterX = (playerPaddle.left + playerPaddle.right) / 2;
            vx += (x - paddleCenterX) * 0.1f; // Ajuste proporcional al impacto

            // Aumentar velocidad tras el rebote
            vx *= speedIncrementFactor;
            vy *= speedIncrementFactor;
        }

        // Asegurar velocidad mínima
        float minSpeed = 5.0f; // Velocidad mínima permitida
        float currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);
        if (currentSpeed < minSpeed) {
            float factor = minSpeed / currentSpeed;
            vx *= factor;
            vy *= factor;
        }

        // Asegurar que la velocidad no se haga excesivamente alta (opcional)
        float maxSpeed = 50.0f; // Velocidad máxima permitida
        currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);
        if (currentSpeed > maxSpeed) {
            float factor = maxSpeed / currentSpeed;
            vx *= factor;
            vy *= factor;
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
