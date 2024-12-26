package es.udc.psi.ttprototipo1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.udc.psi.ttprototipo1.UserInterface.UIHelper;
import es.udc.psi.ttprototipo1.databinding.ActivityLoginBinding;
import es.udc.psi.ttprototipo1.databinding.ActivityMainBinding;
import es.udc.psi.ttprototipo1.databinding.UserLoginDialogBinding;
import es.udc.psi.ttprototipo1.databinding.UserRegisterDialogBinding;
import es.udc.psi.ttprototipo1.databinding.UserSelectUsrDialogBinding;

public class MainActivity extends AppCompatActivity {

    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();
    private UsersFireBaseManagement usersFireBaseManagement = UsersFireBaseManagement.getInstance();

    private ActivityMainBinding binder;
    private View myView;

    private UserLoginDialogBinding loginBinder;
    private View loginView;

    private UserRegisterDialogBinding registerBinder;
    private View registerView;

    private FirebaseAuth mAuth;

    private UIHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // pantalla de inicio al abrir la aplicacion
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binder = ActivityMainBinding.inflate(getLayoutInflater());
        myView = binder.getRoot();

        setContentView(myView);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        Log.d("_TAG","Antes de creacion de UI");

        // Pasar las vistas necesarias a UIHelper
        uiHelper = new UIHelper(this, binder.drawerLayout, binder.navigationView, binder.toolbar);
        uiHelper.setupToolbar();
        uiHelper.setupDrawer();

    }

    @Override
    public void onBackPressed() {
        if (binder.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binder.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        binder.startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("isBottomPlayer", true); // Cambia a false si es top
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            new Handler().postDelayed(() -> binder.startGameButton.setEnabled(true), 500); // Rehabilitar después de 500ms
        });

        View.OnClickListener iListen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == binder.registerButton.getId()) {

                    //alertdialog para registrar nuevo usuario
                    alertRegisterUser();

                } else if (v.getId() == binder.loginButton.getId()) {

                    //alertdialog para iniciar sesion como un usuario concreto
                    alertLogUser();

                } else if (v.getId() == binder.logoutButton.getId()) {

                    if (mAuth.getCurrentUser() != null) {
                        //alertdialog para confirmar cierre de sesión
                        new AlertDialog.Builder(MainActivity.this).setMessage(R.string.logoutdialogtxt).setNegativeButton(R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        }).setPositiveButton(R.string.ok, (dialog, which) -> {
                            logoutUser();
                            binder.loginButton.setEnabled(true);
                            binder.registerButton.setEnabled(true);
                            binder.sendButton.setEnabled(false);
                            binder.userInfo.setText("");
                        }).create().show();
                    } else {
                        Toast.makeText(getApplicationContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
                    }


                } else if (v.getId() == binder.sendButton.getId()) {

                    //alertdialog para mandar mensaje en el edittext
                    if (!binder.messageToSend.getText().toString().trim().isEmpty()) {
                        sendButtonAlert();
                    } else {
                        Toast.makeText(getApplicationContext(), "No message", Toast.LENGTH_SHORT).show();
                    }

                } else if (v.getId() == binder.deleteUserButton.getId()) {
                    //delete user
                    if (mAuth.getCurrentUser() != null) {
                        deleteUserAlert();
                    } else {
                        Toast.makeText(getApplicationContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        binder.registerButton.setOnClickListener(iListen);
        binder.loginButton.setOnClickListener(iListen);
        binder.logoutButton.setOnClickListener(iListen);
        binder.sendButton.setOnClickListener(iListen);
        binder.deleteUserButton.setOnClickListener(iListen);

        listenToChanges();

    }

    @Override
    protected void onResume(){
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            binder.userInfo.setText(getString(R.string.stablishname) + " " + currentUser.getDisplayName() +"\n" + getString(R.string.stablishmail) + " " + currentUser.getEmail());
            binder.loginButton.setEnabled(false);
            binder.registerButton.setEnabled(false);
            rtFireBaseManagement.updateUserConnectionStatus(mAuth.getCurrentUser(), true);
        }else{
            Toast.makeText(getApplicationContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
            binder.sendButton.setEnabled(false);

            Intent goLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(goLogin);
            finish();
        }

    }

    @Override
    protected void onStop(){

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            rtFireBaseManagement.updateUserConnectionStatus(currentUser, false);
        }
        super.onStop();
    }

    private void alertRegisterUser(){
        registerBinder = UserRegisterDialogBinding.inflate(getLayoutInflater());
        registerView = registerBinder.getRoot();

        AlertDialog registerDialog = new AlertDialog.Builder(MainActivity.this).setView(registerView).setMessage(R.string.registerdialogtxt).setPositiveButton(R.string.ok, (dialog, which) ->{
            registerUser(registerBinder.nameSet.getText().toString(), registerBinder.emailSet.getText().toString(), registerBinder.passwSet.getText().toString());
        }).setNegativeButton(R.string.cancel, (dialog, which) ->{
            dialog.dismiss();
        }).create();

        registerBinder.yesAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDialog.dismiss();
                alertLogUser();
            }
        });

        registerDialog.show();
    }

    private void alertLogUser(){
        loginBinder = UserLoginDialogBinding.inflate(getLayoutInflater());
        loginView = loginBinder.getRoot();

        AlertDialog loginDialog = new AlertDialog.Builder(MainActivity.this).setView(loginView).setMessage(R.string.logindialogtxt).setPositiveButton(R.string.ok, (dialog, which) ->{
            loginUser(loginBinder.emailSet.getText().toString(), loginBinder.passwSet.getText().toString());
        }).setNegativeButton(R.string.cancel, (dialog, which) ->{
            dialog.dismiss();
        }).create();

        loginBinder.noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginDialog.dismiss();
                alertRegisterUser();
            }
        });

        loginDialog.show();
    }

    private void loginUser(String email, String password) {
        usersFireBaseManagement.loginUser(email, password, mAuth, MainActivity.this, new UserLoginCallback() {
            @Override
            public void onSuccess() {
                // Inicio de sesión exitoso
                FirebaseUser user = mAuth.getCurrentUser();

                String myName = user.getDisplayName();

                Toast.makeText(MainActivity.this, "Bienvenido, " + myName, Toast.LENGTH_SHORT).show();
                binder.userInfo.setText(getString(R.string.stablishname) + " " + myName +"\n" + getString(R.string.stablishmail) + " " + user.getEmail());
                binder.loginButton.setEnabled(false);
                binder.registerButton.setEnabled(false);
                binder.sendButton.setEnabled(true);
                rtFireBaseManagement.updateUserConnectionStatus(user, true);
                listenToChanges();
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser(){
        usersFireBaseManagement.logoutUser(mAuth.getCurrentUser());
    }


    private void registerUser(String name, String email, String passw){
        mAuth.createUserWithEmailAndPassword(email, passw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser myUser = mAuth.getCurrentUser();

                    UserProfileChangeRequest usrUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name).build();

                    if(myUser != null){

                        myUser.updateProfile(usrUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "registro exitoso", Toast.LENGTH_SHORT).show();
                                    loginUser(email, passw);
                                    rtFireBaseManagement.setUpUserInDatabase(myUser, new UserSetupCallback() {
                                        @Override
                                        public void onSuccessfulTask() {
                                            Toast.makeText(getApplicationContext(), "Usuario guardado en la bd", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailedTask(String errorMsg) {
                                            Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }else{
                                    Toast.makeText(getApplicationContext(), "registro fallido", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "registro fallido " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendButtonAlert(){
        UserSelectUsrDialogBinding usrSelectBinding = UserSelectUsrDialogBinding.inflate(getLayoutInflater());
        View usrSelectView = usrSelectBinding.getRoot();
        ListView listView = usrSelectBinding.userList;

        ArrayList<User> totalUsers = new ArrayList<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.orderByChild("isConnected").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                totalUsers.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    String userId = userSnapshot.child("userId").getValue(String.class);
                    String name = userSnapshot.child("name").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    Boolean isConnected = userSnapshot.child("isConnected").getValue(Boolean.class);
                    Long lastSeen = userSnapshot.child("lastSeen").getValue(Long.class);
                    Boolean available = userSnapshot.child("isAvailable").getValue(Boolean.class);

                    if(userId != null && name != null && email != null && isConnected != null && lastSeen != null && Boolean.TRUE.equals(available)) {
                        if(!userId.equals(mAuth.getCurrentUser().getUid())) {
                            User user = new User(userId, name, email, isConnected, lastSeen);
                            totalUsers.add(user);
                        }
                    }
                }

                final User[] selectedUser = {null};

                if(totalUsers.isEmpty()){
                    Toast.makeText(getApplicationContext(), "no usuarios", Toast.LENGTH_SHORT).show();
                } else {
                    UsersAdapter usersAdapter = new UsersAdapter(totalUsers);
                    listView.setAdapter(usersAdapter);

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        selectedUser[0] = totalUsers.get(position);

                        usrSelectBinding.selectedUser.setText(selectedUser[0].getName());
                    });

                    new AlertDialog.Builder(MainActivity.this).setView(usrSelectView)
                        .setMessage("Seleccione usuario").setPositiveButton("OK", (dialog, which) -> {
                            if(usrSelectBinding.selectedUser.getText().toString().isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                            }else{
                                DatabaseReference neoUserRef = userRef.child(selectedUser[0].getUserId());
                                neoUserRef.child("isWith").setValue(mAuth.getCurrentUser().getUid());
                                neoUserRef.child("message").setValue(binder.messageToSend.getText().toString());
                                neoUserRef.child("isAvailable").setValue(false);
                            }
                        }).setNegativeButton("Cancelar", (dialog, which) ->{
                            dialog.dismiss();
                        }).create().show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void listenToChanges(){
        rtFireBaseManagement.listenToChanges(new ChangesListenCallback() {
            @Override
            public void onManageChanges(String sender, String message, @NonNull DataSnapshot snapshot) {
                if(message != null && sender != null){
                    binder.messageSent.setText(sender + " sent: " + message);

                    snapshot.getRef().child("isWith").setValue("");
                    snapshot.getRef().child("message").setValue("");
                    snapshot.getRef().child("isAvailable").setValue(true);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(){

        FirebaseUser userToDelete = mAuth.getCurrentUser();

        usersFireBaseManagement.deleteUser(userToDelete, new UserDeleteCallback() {
            @Override
            public void onSuccessfulRemove() {
                Toast.makeText(getApplicationContext(), "Usuario borrado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserAlert(){
        new AlertDialog.Builder(this).setMessage("Seguro que quieres borrar el usuario?").setPositiveButton("Ok", ((dialog, which) -> {
            deleteUser();
            binder.loginButton.setEnabled(true);
            binder.registerButton.setEnabled(true);
            binder.sendButton.setEnabled(false);
            binder.userInfo.setText("");
        })).setNegativeButton("Cancelar", ((dialog, which) -> {
            dialog.dismiss();
        })).create().show();
    }

}