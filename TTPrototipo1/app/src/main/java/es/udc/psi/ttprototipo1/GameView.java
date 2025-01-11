package es.udc.psi.ttprototipo1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Canvas;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable,OnDiskExitListener  {
    private Thread gameThread;
    private boolean isPlaying;
    private Canvas canvas;
    private Paint paint;
    private long previousTime;
    private Disk disk;

    private String matchId;
    private String playerId;
    private FirebaseAuth mAuth;
    private float x = 200, y = 200, radius = 50; // Posición y tamaño del disco
    private Paint paint2;        // Pintura para dibujar
    private RectF playerPaddle; // Pala del jugador
    private RectF opponentPaddle; // Pala del oponente
    private float paddleWidth = 200; // Ancho de la pala
    private float paddleHeight = 30; // Altura de la pala
    private boolean isBottomPlayer; // Indica si el jugador está en la posición inferior
    private boolean isDiskVisible;

    private Paint scorePaint; // Pintura para el texto del puntaje
    private int score = 5;

    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();


    public GameView(Context context, String matchId, boolean isBottomPlayer) {
        super(context);
        this.isBottomPlayer = isBottomPlayer;
        this.matchId = matchId;
        getHolder().addCallback(this);
        paint = new Paint();
        disk = new Disk(x, y, radius); // Inicializa el disco
        if(isBottomPlayer){
            isDiskVisible = true; // Asegura que el disco sea visible al inicio
        }else{
            isDiskVisible = false;
        }
        mAuth = FirebaseAuth.getInstance();
        playerId = mAuth.getCurrentUser().getUid();
        rtFireBaseManagement.detectDiskChanges(playerId, matchId, new DiskChangeDetectionCallback() {
            @Override
            public void onCangeDetected(float x, float y, float vx, float vy) {
                if(x<0+radius){x=0+radius;}
                if(x>getWidth()-radius){x=getWidth()-radius;}
                if(y<0+radius){y=0+radius;}
                if(y>getHeight()-radius){y=getHeight()-radius;}
                receiveDisk(getWidth()-x, y, (float)-1.0*vx, (float)-1.0*vy);
            }

            @Override
            public void onFail(String errorMsg) {
                stopGame();
            }
        });

        scorePaint = new Paint(); // Inicializar el Paint para el puntaje
        scorePaint.setColor(Color.WHITE); // Color del texto
        scorePaint.setTextSize(50); // Tamaño del texto
        scorePaint.setAntiAlias(true); // Suavizado del texto
        scorePaint.setFakeBoldText(true); // Negrita

    }

    public void stopGame() {
        isPlaying = false;
        try {
            if (gameThread != null) {
                gameThread.join(); // Detén el hilo
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isPlaying = true;
        previousTime = System.currentTimeMillis();
        gameThread = new Thread(this);
        gameThread.start();

        // Pala del jugador en la parte inferior
        playerPaddle = new RectF(
            getWidth() / 2 - paddleWidth / 2,
            getHeight() - 100,
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
        if (isDiskVisible && disk != null) {
            boolean isInPlay = disk.update(deltaTime, getHeight(), getWidth(), playerPaddle, isBottomPlayer, this);
            if (!isInPlay) {
                isDiskVisible = false;
            }
        }
    }




    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();

            if(canvas != null){
                canvas.drawColor(Color.BLACK);
                // Dibuja el disco solo si está visible
                if (isDiskVisible) {
                    disk.draw(canvas, paint);
                }

                // Dibuja la pala del jugador
                paint.setColor(Color.WHITE);
                canvas.drawRect(playerPaddle, paint);

                // Dibuja el puntaje en la esquina superior izquierda
                String scoreText = "Score: " + score; // Formato del puntaje
                canvas.drawText(scoreText, 50, 100, scorePaint); // Posición (50, 100)

                getHolder().unlockCanvasAndPost(canvas);

            }

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


    @Override
    public void onDiskExit(float x, float y, float angle, String sender) {
        // Manejar la salida del disco
        isDiskVisible = false;

        if(y - radius < 0){
            rtFireBaseManagement.changeDisk(matchId, x, y, disk.getVx(), disk.getVY());
        }else if (y + radius > getHeight()){

            receiveDisk(getWidth() / 2f, getHeight() / 2f, 0, 5);

            rtFireBaseManagement.updateScore(playerId, matchId, score-1);
            score--;

        }
    }

    public void receiveDisk(float newX, float newY, float newVx, float newVy) {

        // Hacer visible el disco nuevamente
        isDiskVisible = true;

        // Actualiza la posición y velocidad del disco
        disk.setPosition(newX, newY);
        disk.setVelocity(newVx, newVy);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Libera recursos como hilos, timers, etc.
        stopGame();
    }

}
