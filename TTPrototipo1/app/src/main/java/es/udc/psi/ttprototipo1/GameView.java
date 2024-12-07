package es.udc.psi.ttprototipo1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Canvas;
import androidx.annotation.NonNull;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private Canvas canvas;
    private Paint paint;
    private long previousTime;
    private Disk disk;

    private float x, y, radius; // Posición y tamaño del disco
    private Paint paint2;        // Pintura para dibujar
    private RectF playerPaddle; // Pala del jugador
    private RectF opponentPaddle; // Pala del oponente
    private float paddleWidth = 200; // Ancho de la pala
    private float paddleHeight = 30; // Altura de la pala



    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        paint = new Paint();
        disk = new Disk(200, 200, 50); // Disco inicial
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isPlaying = true;
        previousTime = System.currentTimeMillis();
        gameThread = new Thread(this);
        gameThread.start();

        // Inicializar la pala en la parte inferior de la pantalla
        playerPaddle = new RectF(
            getWidth() / 2 - paddleWidth / 2, // Centrada horizontalmente
            getHeight() - 100,               // Cerca del borde inferior
            getWidth() / 2 + paddleWidth / 2,
            getHeight() - 70
        );
    }



    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isPlaying) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - previousTime;
            previousTime = currentTime;

            update(deltaTime);
            draw();
        }
    }

    private void update(long deltaTime) {
        disk.update(deltaTime, playerPaddle);
    }



    public void draw() {
        if (getHolder().getSurface().isValid()) {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK); // Fondo negro

            // Dibuja el disco
            disk.draw(canvas, paint);

            // Dibuja la pala del jugador
            paint.setColor(Color.WHITE);
            canvas.drawRect(playerPaddle, paint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float touchX = event.getX();
            float halfWidth = paddleWidth / 2;

            // Actualiza la posición de la pala, limitando dentro de los bordes
            playerPaddle.left = Math.max(0, touchX - halfWidth);
            playerPaddle.right = Math.min(getWidth(), touchX + halfWidth);
        }
        return true;
    }


}
