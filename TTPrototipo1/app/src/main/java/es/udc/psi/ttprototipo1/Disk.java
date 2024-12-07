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

    public void update(long deltaTime, RectF playerPaddle) {
        // Actualiza la posición del disco
        x += vx * deltaTime / 16;
        y += vy * deltaTime / 16;

        // Rebote en los bordes
        if (x - radius < 0 || x + radius > 1080) vx = -vx; // Cambia el ancho según tu resolución
        if (y - radius < 0) vy = -vy; // Rebote solo en el borde superior

        // Detectar colisión con la pala del jugador
        if (RectF.intersects(playerPaddle, new RectF(x - radius, y - radius, x + radius, y + radius))) {
            vy = -vy; // Rebote en el eje Y
            // Ajustar dirección en el eje X según el punto de impacto
            vx += (x - (playerPaddle.left + playerPaddle.right) / 2) * 0.05f;
            vx = Math.max(-maxSpeed, Math.min(maxSpeed, vx));
            vy = Math.max(-maxSpeed, Math.min(maxSpeed, vy));

        }
    }


    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
    }
}
