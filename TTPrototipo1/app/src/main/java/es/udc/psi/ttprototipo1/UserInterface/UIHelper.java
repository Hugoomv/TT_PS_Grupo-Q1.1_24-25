package es.udc.psi.ttprototipo1.UserInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import es.udc.psi.ttprototipo1.MainActivity;
import es.udc.psi.ttprototipo1.R;
import es.udc.psi.ttprototipo1.SettingsActivity;
import es.udc.psi.ttprototipo1.databinding.ActivityMainBinding;
import es.udc.psi.ttprototipo1.databinding.NavHeaderBinding;

public class UIHelper {

    private final Context context;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final Toolbar toolbar;
    private final String username;

    private static final int PICK_IMAGE_REQUEST = 1;

    public UIHelper(Context context, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar, String username) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toolbar = toolbar;
        this.username = username;
    }

    public void setupUI(){
        setupToolbar();
        setupDrawer();
    }

    public void setupToolbar() {
        toolbar.setTitle(context.getString(R.string.app_name));
    }

    public void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            (AppCompatActivity) context, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupNavigationView();
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            handleMenuItemClick(menuItem);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        setDefaultMenuItem();
        setupHeader();
    }

    private void setDefaultMenuItem() {
        MenuItem menuItem = navigationView.getMenu().getItem(0);
        menuItem.setChecked(true);
    }

    private void setupHeader() {
        // crear binding
        NavHeaderBinding headerBinding = NavHeaderBinding.bind(
            navigationView.getHeaderView(0)
        );

        // Presionar en el header del menu para cambiar la imagen de perfil
        headerBinding.headerTextView.setOnClickListener(v -> openImagePicker());
        headerBinding.headerImage.setOnClickListener(v -> openImagePicker());
        headerBinding.headerTitle.setOnClickListener(v -> openImagePicker());

        // Cargar la imagen guardada usando ProfileImageManager
        Bitmap profileImage = ProfileImage.loadProfileImage(context);
        if (profileImage != null) {
            headerBinding.headerImage.setImageBitmap(profileImage);
        }

        // Nombre del usuario
        headerBinding.headerTextView.setText(username);
    }

    private void handleMenuItemClick(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_settings) { // Abrir ajustes
            if (context instanceof MainActivity) {
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
            } else {
                throw new IllegalStateException(context.getString(R.string.invalid_context));
            }
        }else if(id == R.id.nav_exit) { // Para salir de la app
            if (context instanceof Activity) {
                ((Activity) context).finishAffinity(); // Cierra la pila de tareas
                System.exit(0);
            }
        } else {
            Toast.makeText(context, context.getString(R.string.no_func), Toast.LENGTH_SHORT).show();
        }
    }

    // Abrir selector de imágenes
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }
}
