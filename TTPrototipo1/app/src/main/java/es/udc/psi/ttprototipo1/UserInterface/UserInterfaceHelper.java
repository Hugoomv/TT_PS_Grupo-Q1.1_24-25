package es.udc.psi.ttprototipo1.UserInterface;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import es.udc.psi.ttprototipo1.R;
import es.udc.psi.ttprototipo1.databinding.ActivityMainBinding;

public class UserInterfaceHelper {

    private final AppCompatActivity activity;
    private final ActivityMainBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;

    public UserInterfaceHelper(AppCompatActivity activity, ActivityMainBinding binding) {
        this.activity = activity;
        this.binding = binding;
    }

    public void createUI() {

        createMenuLateral();

    }

    private void createMenuLateral(){
        // Configurar el toggle para la barra de acción
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar, // Usa el toolbar del binding
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Habilitar el botón de navegación en la barra superior
        toggle.setDrawerSlideAnimationEnabled(true);
        Objects.requireNonNull(activity.getSupportActionBar()).setHomeButtonEnabled(true);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configurar el NavigationView
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_settings) {
                Log.d("_TAG", "MainActivity Menu_settings");
                // Acción para "Configuración"
            } else if (id == R.id.nav_exit) {
                Log.d("_TAG", "MainActivity Menu_exit");
                activity.finish(); // Cierra la actividad
            }

            binding.drawerLayout.closeDrawer(GravityCompat.START); // Cierra el menú después de seleccionar
            return true;
        });
    }


    public boolean handleDrawerToggle(MenuItem item) {
        if (binding.drawerLayout != null && item.getItemId() == android.R.id.home) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return false; // Devuelve false si no se maneja el evento
    }

}
