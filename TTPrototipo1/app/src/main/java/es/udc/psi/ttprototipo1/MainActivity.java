package es.udc.psi.ttprototipo1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import es.udc.psi.ttprototipo1.UserInterface.UIHelper;
import es.udc.psi.ttprototipo1.databinding.ActivityMainBinding;
import es.udc.psi.ttprototipo1.databinding.UserSelectUsrDialogBinding;

public class MainActivity extends AppCompatActivity {

    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();
    private UsersFireBaseManagement usersFireBaseManagement = UsersFireBaseManagement.getInstance();

    private ActivityMainBinding binder;
    private View myView;

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

        mAuth = FirebaseAuth.getInstance();

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
                if (v.getId() == binder.logoutButton.getId()) {

                    if (mAuth.getCurrentUser() != null) {
                        //alertdialog para confirmar cierre de sesión
                        new AlertDialog.Builder(MainActivity.this).setMessage(R.string.logoutdialogtxt).setNegativeButton(R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        }).setPositiveButton(R.string.ok, (dialog, which) -> {
                            logoutUser();
                            binder.userInfo.setText("");
                        }).create().show();
                    } else {
                        Toast.makeText(getApplicationContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
                    }


                } else if (v.getId() == binder.sendButton.getId()) {

                    //alertdialog para mandar mensaje en el edittext
                    sendButtonAlert();

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
            rtFireBaseManagement.updateUserConnectionStatus(currentUser, true);
        }else{
            Toast.makeText(getApplicationContext(), "You are not logged in", Toast.LENGTH_SHORT).show();

            Intent goLogin = new Intent(MainActivity.this, LoginActivity.class);
            goLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goLogin);
            finish();
        }

    }

    @Override
    protected void onPause(){

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            rtFireBaseManagement.updateUserConnectionStatus(currentUser, false);
        }
        rtFireBaseManagement.stopListeningToChanges();

        super.onPause();
    }

    private void logoutUser(){
        usersFireBaseManagement.logoutUser(mAuth.getCurrentUser());
        Intent goLogin = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(goLogin);
        finish();
    }

    private void sendButtonAlert(){
        UserSelectUsrDialogBinding usrSelectBinding = UserSelectUsrDialogBinding.inflate(getLayoutInflater());
        View usrSelectView = usrSelectBinding.getRoot();
        ListView listView = usrSelectBinding.userList;

        rtFireBaseManagement.connectedUsers(mAuth.getCurrentUser(), new UsersConnectedCallback() {
            @Override
            public void onUsersLoaded(ArrayList<User> users) {

                final User[] selectedUser = {null};

                if(users.isEmpty()){
                    Toast.makeText(getApplicationContext(), "no usuarios", Toast.LENGTH_SHORT).show();
                } else {
                    UsersAdapter usersAdapter = new UsersAdapter(users);
                    listView.setAdapter(usersAdapter);

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        selectedUser[0] = users.get(position);

                        usrSelectBinding.selectedUser.setText(selectedUser[0].getName());
                    });

                    new AlertDialog.Builder(MainActivity.this).setView(usrSelectView)
                        .setMessage("Seleccione usuario").setPositiveButton("OK", (dialog, which) -> {
                            if(usrSelectBinding.selectedUser.getText().toString().isEmpty()) {
                                Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                            }else{

                                String newMatchId = mAuth.getCurrentUser().getUid()+selectedUser[0].getUserId();
                                rtFireBaseManagement.createMatch(mAuth.getCurrentUser().getUid(), selectedUser[0].getUserId(), new MatchCreationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        rtFireBaseManagement.sendMessage(mAuth.getCurrentUser(), selectedUser[0].getUserId(), "invite");
                                    }

                                    @Override
                                    public void onFail(String errorMsg) {
                                        Toast.makeText(getApplicationContext(), "Error while creating match", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancelar", (dialog, which) ->{
                            dialog.dismiss();
                        }).create().show();
                }
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void managePetition(String senderId, String message){
        AlertDialog invite = new AlertDialog.Builder(this).setMessage(message + ". Aceptar?").setPositiveButton("Ok", ((dialog, which) -> {

            rtFireBaseManagement.sendMessage(mAuth.getCurrentUser(), senderId, "accept");

            String newMatchId = senderId+mAuth.getCurrentUser().getUid();
            Intent matchAct = new Intent(MainActivity.this, GameActivity.class);
            matchAct.putExtra(Intent.EXTRA_TEXT, newMatchId);
            matchAct.putExtra("isBottomPlayer", false); // Cambia a false si es top
            matchAct.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(matchAct);

        })).setNegativeButton("Cancelar", ((dialog, which) -> {
            Toast.makeText(getApplicationContext(), "Invite rejected", Toast.LENGTH_SHORT).show();
            rtFireBaseManagement.sendMessage(mAuth.getCurrentUser(), senderId, "deny");
        })).setCancelable(false).create();

        invite.show();
    }

    private void listenToChanges(){
        rtFireBaseManagement.listenToChanges(new ChangesListenCallback() {
            @Override
            public void onManageChanges(String sender, String senderId, String message) {
                if(message != null && sender != null){

                    if(message.equals("invite")){
                        managePetition(senderId, sender + " sent " + message);
                    }if(message.equals("accept")){
                        String newMatchId = mAuth.getCurrentUser().getUid()+senderId;
                        Intent matchAct = new Intent(MainActivity.this, GameActivity.class);
                        matchAct.putExtra(Intent.EXTRA_TEXT, newMatchId);
                        matchAct.putExtra("isBottomPlayer", true); // Cambia a false si es top
                        matchAct.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(matchAct);
                    }else if(message.equals("deny")){
                        Toast.makeText(getApplicationContext(), "invite was rejected", Toast.LENGTH_LONG).show();
                    }

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

                Intent goLogin = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(goLogin);
                finish();
            }

            @Override
            public void onFailedRemove() {
                Toast.makeText(getApplicationContext(), "vuelve a iniciar sesión para poder borrar la cuenta", Toast.LENGTH_SHORT).show();
                logoutUser();
            }
        });
    }

    private void deleteUserAlert(){
        new AlertDialog.Builder(this).setMessage("Seguro que quieres borrar el usuario?").setPositiveButton("Ok", ((dialog, which) -> {
            deleteUser();
            binder.userInfo.setText("");
        })).setNegativeButton("Cancelar", ((dialog, which) -> {
            dialog.dismiss();
        })).create().show();
    }

}