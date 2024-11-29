package es.udc.psi.ttprototipo1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Disk {
    private float x, y, radius;
    private float vx = 5, vy = 5; // Velocidades iniciales
    private final float friction = 0.00f;

    public Disk(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public void update(long deltaTime) {
        x += vx * deltaTime / 16; // Normalizado a ~60 FPS
        y += vy * deltaTime / 16;

        // Aplica fricción
        vx *= (1 - friction);
        vy *= (1 - friction);

        // Rebotar en los bordes
        if (x - radius < 0 || x + radius > 1080) vx = -vx; // Cambia el ancho según tu resolución
        if (y - radius < 0 || y + radius > 1920) vy = -vy; // Cambia la altura según tu resolución
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
    }
}
