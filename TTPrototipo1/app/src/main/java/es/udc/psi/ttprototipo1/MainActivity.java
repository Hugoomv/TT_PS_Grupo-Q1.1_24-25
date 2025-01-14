package es.udc.psi.ttprototipo1;


import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.util.ArrayList;

import es.udc.psi.ttprototipo1.UserInterface.ProfileImage;
import es.udc.psi.ttprototipo1.UserInterface.UIHelper;
import es.udc.psi.ttprototipo1.databinding.ActivityMainBinding;

import es.udc.psi.ttprototipo1.databinding.NavHeaderBinding;

import es.udc.psi.ttprototipo1.databinding.UserSelectUsrDialogBinding;


//TODO splash screen

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();

    private ActivityMainBinding binder;
    private View myView;

    private FirebaseAuth mAuth;

    private UIHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Instalar la Splash Screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

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
        String username;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Si el usuario no está logueado, el user puede ser null
        if(currentUser == null){
            username = "Default";
        }
        else {
            username = currentUser.getDisplayName();
        }

        // Pasar las vistas necesarias a UIHelper
        uiHelper = new UIHelper(this, binder.drawerLayout, binder.navigationView, binder.toolbar, username);
        uiHelper.setupUI();

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

        View.OnClickListener iListen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == binder.sendButton.getId()) {

                    //alertdialog para mandar mensaje en el edittext
                    sendButtonAlert();

                }
            }
        };

        binder.sendButton.setOnClickListener(iListen);

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
            Toast.makeText(getApplicationContext(), R.string.notloggedinmsg, Toast.LENGTH_SHORT).show();

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

    private void sendButtonAlert(){
        UserSelectUsrDialogBinding usrSelectBinding = UserSelectUsrDialogBinding.inflate(getLayoutInflater());
        View usrSelectView = usrSelectBinding.getRoot();
        ListView listView = usrSelectBinding.userList;

        rtFireBaseManagement.connectedUsers(mAuth.getCurrentUser(), new UsersConnectedCallback() {
            @Override
            public void onUsersLoaded(ArrayList<User> users) {

                final User[] selectedUser = {null};

                if(users.isEmpty()){
                    Toast.makeText(getApplicationContext(), R.string.nousersmsg, Toast.LENGTH_SHORT).show();
                } else {
                    UsersAdapter usersAdapter = new UsersAdapter(users);
                    listView.setAdapter(usersAdapter);

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        selectedUser[0] = users.get(position);

                        usrSelectBinding.selectedUser.setText(selectedUser[0].getName());
                    });

                    new AlertDialog.Builder(MainActivity.this).setView(usrSelectView)
                        .setMessage(R.string.selectuserdialogtxt).setPositiveButton(R.string.ok, (dialog, which) -> {
                            if(usrSelectBinding.selectedUser.getText().toString().isEmpty()) {
                                Toast.makeText(getApplicationContext(), R.string.nouserselectedmsg, Toast.LENGTH_SHORT).show();
                            }else{

                                String newMatchId = mAuth.getCurrentUser().getUid()+selectedUser[0].getUserId();
                                rtFireBaseManagement.createMatch(mAuth.getCurrentUser().getUid(), selectedUser[0].getUserId(), new MatchCreationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        rtFireBaseManagement.sendMessage(mAuth.getCurrentUser(), selectedUser[0].getUserId(), "invite");
                                    }

                                    @Override
                                    public void onFail(String errorMsg) {
                                        Toast.makeText(getApplicationContext(), getText(R.string.matchnotcreatedmsg) + ": " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).setNegativeButton(R.string.cancel, (dialog, which) ->{
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
        AlertDialog invite = new AlertDialog.Builder(this).setMessage(message + ". " + getText(R.string.acceptinvitequestiontxt)).setPositiveButton(R.string.ok, ((dialog, which) -> {

            rtFireBaseManagement.sendMessage(mAuth.getCurrentUser(), senderId, "accept");

            String newMatchId = senderId+mAuth.getCurrentUser().getUid();
            Intent matchAct = new Intent(MainActivity.this, GameActivity.class);
            matchAct.putExtra(Intent.EXTRA_TEXT, newMatchId);
            matchAct.putExtra("isBottomPlayer", false); // Cambia a false si es top
            matchAct.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(matchAct);

        })).setNegativeButton(R.string.cancel, ((dialog, which) -> {
            Toast.makeText(getApplicationContext(), R.string.inviterejectedmsg, Toast.LENGTH_SHORT).show();
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
                        rtFireBaseManagement.deleteMatch(mAuth.getCurrentUser().getUid() + senderId, new MatchDeleteCallback() {
                            @Override
                            public void onSuccessfulRemove() {
                                Toast.makeText(getApplicationContext(), R.string.inviterejectedmsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void checkRanking(){
        rtFireBaseManagement.topTen(mAuth.getCurrentUser(), new UsersConnectedCallback() {
            @Override
            public void onUsersLoaded(ArrayList<User> users) {
                /*
                *   usar estas funciones, las demás darán datos vacíos en este caso
                *   public String getName() { return name; }
                *   public String getEmail() { return email; }
                *   public int getMatchesPlayed() { return matchesPlayed; }
                *   public int getMatchesWon(){ return matchesWon; }
                *
                */
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            if(selectedImageUri != null) {
                try {
                    // Log de URI para verificar

                    // Convertir la URI en un InputStream para decodificarla
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    // Verificar si se obtiene una imagen válida
                    if (selectedImage != null) {

                        // Mostrar la imagen en el ImageView

                        NavHeaderBinding headerBinding = NavHeaderBinding.bind(binder.navigationView.getHeaderView(0));
                        headerBinding.headerImage.setImageBitmap(selectedImage);

                        // Aquí podrías agregar la lógica para guardar la imagen
                        ProfileImage.saveProfileImage(this, selectedImage);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.image_error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("_TAG", "Error al cargar la imagen: " + e.getMessage());
                }
            }
        }
    }

}