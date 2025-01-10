package es.udc.psi.ttprototipo1.UserInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.udc.psi.ttprototipo1.MainActivity;
import es.udc.psi.ttprototipo1.R;
import es.udc.psi.ttprototipo1.SettingsActivity;
import es.udc.psi.ttprototipo1.databinding.NavHeaderBinding;

public class UIHelper {

    private final Context context;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final Toolbar toolbar;
    private final String username;

    public UIHelper(Context context, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar, String username) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toolbar = toolbar;
        this.username = username;
    }

    public void setupToolbar() {
        toolbar.setTitle(context.getString(R.string.app_name)); // Opcional: Cambiar título
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
        handleMenuItemClick(menuItem);
    }

    private void setupHeader() {
        // Usar Binding para acceder al encabezado
        NavHeaderBinding headerBinding = NavHeaderBinding.bind(
            navigationView.getHeaderView(0)
        );
        headerBinding.headerTitle.setOnClickListener(view -> Toast.makeText(
            context,
            context.getString(R.string.title_click),
            Toast.LENGTH_SHORT
        ).show());

        headerBinding.headerTextView.setText(username);

    }

    private void handleMenuItemClick(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_settings) {
            if (context instanceof MainActivity) {
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
            } else {
                throw new IllegalStateException("El contexto no es una instancia de Activity");
            }
        }else {
            Toast.makeText(context, context.getString(getTitle(menuItem)), Toast.LENGTH_SHORT).show();
        }
    }


    private int getTitle(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_settings) {
            return R.string.menu_ajustes;
        } else if (id == R.id.nav_exit) {
            return R.string.menu_salir;
        } else {
            throw new IllegalArgumentException("menu option not implemented!!");
        }
    }
}
