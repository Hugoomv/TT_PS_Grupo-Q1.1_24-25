package es.udc.psi.ttprototipo1.UserInterface;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.udc.psi.ttprototipo1.LoginActivity;
import es.udc.psi.ttprototipo1.MainActivity;
import es.udc.psi.ttprototipo1.R;
import es.udc.psi.ttprototipo1.SettingsActivity;
import es.udc.psi.ttprototipo1.UserDeleteCallback;
import es.udc.psi.ttprototipo1.UsersFireBaseManagement;
import es.udc.psi.ttprototipo1.databinding.ActivityMainBinding;
import es.udc.psi.ttprototipo1.databinding.ConfirmDeleteDialogBinding;
import es.udc.psi.ttprototipo1.databinding.NavHeaderBinding;

public class UIHelper {

    private final Context context;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final Toolbar toolbar;
    private final String username;

    private UsersFireBaseManagement usersFireBaseManagement = UsersFireBaseManagement.getInstance();

    private static final int PICK_IMAGE_REQUEST = 1;

    public UIHelper(Context context, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar, String username) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toolbar = toolbar;
        this.username = username;
    }

    public void setupUI(){

        // Aplicar tema guardado en SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isDarkMode = preferences.getBoolean("pref_theme", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Crear menú lateral y toolbar
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
                ((Activity) context).startActivity(intent);
            } else {
                throw new IllegalStateException(context.getString(R.string.invalid_context));
            }
        }else if(id == R.id.nav_exit) { // Para salir de la app
            if (context instanceof Activity) {
                ((Activity) context).finishAffinity(); // Cierra la pila de tareas
                System.exit(0);
            }
        }else if(id == R.id.log_out){
            if(context instanceof MainActivity){
                alertLogout();
            }
        }else if(id == R.id.delete_user) {
            if(context instanceof MainActivity){
                deleteUserAlert();
            }
        }else {
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

    private void logoutUser(){
        usersFireBaseManagement.logoutUser(FirebaseAuth.getInstance().getCurrentUser());
        Intent goLogin = new Intent(context, LoginActivity.class);
        ((Activity)context).startActivity(goLogin);
        ((Activity)context).finish();
    }

    private void alertLogout(){

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //alertdialog para confirmar cierre de sesión
            new AlertDialog.Builder(context).setMessage(R.string.logoutdialogtxt).setNegativeButton(R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
            }).setPositiveButton(R.string.ok, (dialog, which) -> {
                logoutUser();
            }).create().show();
        } else {
            Toast.makeText(context, R.string.notloggedinmsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUser(String password){

        FirebaseUser userToDelete = FirebaseAuth.getInstance().getCurrentUser();

        usersFireBaseManagement.deleteUser(userToDelete, password, new UserDeleteCallback() {
            @Override
            public void onSuccessfulRemove() {
                Toast.makeText(context, R.string.userdeletedmsg, Toast.LENGTH_SHORT).show();

                Intent goLogin = new Intent(context, LoginActivity.class);
                ((Activity)context).startActivity(goLogin);
                ((Activity)context).finish();
            }

            @Override
            public void onFailedRemove(String error) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteUserAlert(){

        ConfirmDeleteDialogBinding confirmBinder = ConfirmDeleteDialogBinding.inflate(((Activity)context).getLayoutInflater());
        View confirmView = confirmBinder.getRoot();

        new AlertDialog.Builder(context).setMessage(R.string.deleteuserdialogtxt).setView(confirmView).setPositiveButton(R.string.ok, ((dialog, which) -> {
            deleteUser(confirmBinder.reloginPass.getText().toString());
        })).setNegativeButton(R.string.cancel, ((dialog, which) -> {
            dialog.dismiss();
        })).create().show();
    }
}
