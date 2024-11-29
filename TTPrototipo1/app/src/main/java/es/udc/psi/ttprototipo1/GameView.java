package es.udc.psi.ttprototipo1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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
        disk.update(deltaTime);
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK); // Fondo negro
            disk.draw(canvas, paint);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
}
