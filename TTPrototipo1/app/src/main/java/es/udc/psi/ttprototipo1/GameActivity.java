package es.udc.psi.ttprototipo1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import es.udc.psi.ttprototipo1.databinding.GameActivityBinding;

public class GameActivity extends AppCompatActivity {

    private GameActivityBinding binder;

    private String matchId;

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

        String matchId = receibedIntent.getStringExtra(Intent.EXTRA_TEXT);

        // Obtener el valor de "isBottomPlayer" del Intent
        boolean isBottomPlayer = getIntent().getBooleanExtra("isBottomPlayer", true);

        //añadir al intent el valor de isbottonplayer al crear la partida
        // Crear una instancia de GameView y añadirla al contenedor
        GameView gameView = new GameView(this, isBottomPlayer); // true para jugador inferior
        binder.gameViewContainer.addView(gameView);

    }

    // Manejar el clic en el botón de retroceso
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Este método cierra la actividad y regresa a la anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binder.gameViewContainer.removeAllViews(); // Limpia las vistas del contenedor
    }

}
