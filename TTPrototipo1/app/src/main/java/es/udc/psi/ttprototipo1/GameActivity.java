package es.udc.psi.ttprototipo1;


import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import es.udc.psi.ttprototipo1.databinding.GameActivityBinding;

public class GameActivity extends AppCompatActivity {

    private GameActivityBinding binder;

    private String matchId;
    private boolean isBottomPlayer;

    private boolean matchConcluded;


    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el layout usando View Binding
        binder = GameActivityBinding.inflate(getLayoutInflater());
        setContentView(binder.getRoot());

        // Configurar la Toolbar y el botón de retroceso
        Toolbar toolbar = binder.toolbar; // Usamos el binding para acceder a la Toolbar
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilitar el botón de retroceso
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Mostrar la flecha de retroceso
        }

        Intent receibedIntent = getIntent();

        this.matchId = receibedIntent.getStringExtra(Intent.EXTRA_TEXT);

        // Obtener el valor de "isBottomPlayer" del Intent
        isBottomPlayer = getIntent().getBooleanExtra("isBottomPlayer", true);

        //añadir al intent el valor de isbottonplayer al crear la partida
        // Crear una instancia de GameView y añadirla al contenedor
        GameView gameView = new GameView(this, this.matchId, isBottomPlayer); // true para jugador inferior
        binder.gameViewContainer.addView(gameView);

        matchConcluded = false;

        rtFireBaseManagement.controlWinner(matchId, new WinnerCallback() {
            @Override
            public void declareWinner(String id) {
                if(id.equals(FirebaseAuth.getInstance().getUid())){
                    Toast.makeText(getApplicationContext(), "You win!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "You lose!", Toast.LENGTH_LONG).show();
                }

                rtFireBaseManagement.stopDetectingDiskChanges();
                rtFireBaseManagement.stopControlingScore();
                rtFireBaseManagement.stopControlingWinner();

                matchConcluded = true;
                finish();

            }
        });

        rtFireBaseManagement.controlScore(matchId, FirebaseAuth.getInstance().getUid());
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed(){
        if(!matchConcluded){
            rtFireBaseManagement.forfeit(matchId, FirebaseAuth.getInstance().getUid());

            Toast.makeText(getApplicationContext(), "Forfeited", Toast.LENGTH_SHORT).show();
        }
    }

    // Manejar el clic en el botón de retroceso
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        binder.gameViewContainer.removeAllViews(); // Limpia las vistas del contenedor


        if(isBottomPlayer){
            rtFireBaseManagement.deleteMatch(this.matchId, new MatchDeleteCallback() {
                @Override
                public void onSuccessfulRemove() {
                    Toast.makeText(getApplicationContext(), "terminando partida...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        super.onDestroy();
    }

}
