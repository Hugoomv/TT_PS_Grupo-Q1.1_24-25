package es.udc.psi.ttprototipo1.UserInterface;

import android.content.Context;
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

import es.udc.psi.ttprototipo1.R;
import es.udc.psi.ttprototipo1.databinding.NavHeaderBinding;

public class UIHelper {

    private final Context context;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final Toolbar toolbar;

    public UIHelper(Context context, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toolbar = toolbar;
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

        Log.d("_TAG","Creando UI");
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
        // Usar ViewBinding para acceder al encabezado
        NavHeaderBinding headerBinding = NavHeaderBinding.bind(
            navigationView.getHeaderView(0)
        );
        headerBinding.headerTitle.setOnClickListener(view -> Toast.makeText(
            context,
            context.getString(R.string.title_click),
            Toast.LENGTH_SHORT
        ).show());
    }

    private void handleMenuItemClick(@NonNull MenuItem menuItem) {
        // Aquí puedes manejar las acciones de cada ítem del menú
        Toast.makeText(context, context.getString(getTitle(menuItem)), Toast.LENGTH_SHORT).show();
    }

    private int getTitle(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_camera){
            return R.string.menu_camera;
        } else if(id == R.id.nav_gallery){
            return R.string.menu_gallery;
        }else if (id == R.id.nav_settings) {
            return R.string.menu_ajustes;
        } else if (id == R.id.nav_exit) {
            return R.string.menu_salir;
        } else {
            throw new IllegalArgumentException("menu option not implemented!!");
        }
    }
}
