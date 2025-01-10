package es.udc.psi.ttprototipo1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import es.udc.psi.ttprototipo1.databinding.ActivityLoginBinding;
import es.udc.psi.ttprototipo1.databinding.UserLoginDialogBinding;
import es.udc.psi.ttprototipo1.databinding.UserRegisterDialogBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binder;
    private View mainView;

    private UserLoginDialogBinding loginBinder;
    private View loginView;

    private UserRegisterDialogBinding registerBinder;
    private View registerView;

    private FirebaseAuth mAuth;

    RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();
    UsersFireBaseManagement usersFireBaseManagement = UsersFireBaseManagement.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binder = ActivityLoginBinding.inflate(getLayoutInflater());
        mainView = binder.getRoot();

        setContentView(mainView);

        mAuth = FirebaseAuth.getInstance();

        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/


    }

    @Override
    protected void onStart() {
        super.onStart();

        View.OnClickListener iListen = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == binder.registerScreenButton.getId()) {

                    //alertdialog para registrar nuevo usuario
                    alertRegisterUser();

                } else if (v.getId() == binder.loginScreenButton.getId()) {

                    //alertdialog para iniciar sesion como un usuario concreto
                    alertLogUser();

                }
            }
        };

        binder.registerScreenButton.setOnClickListener(iListen);
        binder.loginScreenButton.setOnClickListener(iListen);

    }

    private void registerUser(String name, String email, String password){
        usersFireBaseManagement.registerUser(name, email, password, mAuth, new UserSetupCallback() {
            @Override
            public void onSuccessfulTask() {

                Toast.makeText(getApplicationContext(), R.string.successfulregistrymsg, Toast.LENGTH_SHORT).show();

                logUser(email, password);

                rtFireBaseManagement.setUpUserInDatabase(mAuth.getCurrentUser(), new UserSetupCallback() {
                    @Override
                    public void onSuccessfulTask() {
                        Toast.makeText(getApplicationContext(), R.string.userindbmsg, Toast.LENGTH_SHORT).show();

                        finish();
                    }

                    @Override
                    public void onFailedTask(String errorMsg) {
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFailedTask(String errorMsg) {
                Toast.makeText(getApplicationContext(), R.string.failedregistrymsg + ": " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logUser(String email, String password){
        usersFireBaseManagement.loginUser(email, password, mAuth, LoginActivity.this, new UserLoginCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser user = mAuth.getCurrentUser();

                String myName = user.getDisplayName();

                Toast.makeText(getApplicationContext(), getText(R.string.welcomemsg) + ", " + myName, Toast.LENGTH_SHORT).show();

                Intent returnToSender = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(returnToSender);
                finish();
            }

            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void alertRegisterUser(){
        registerBinder = UserRegisterDialogBinding.inflate(getLayoutInflater());
        registerView = registerBinder.getRoot();

        AlertDialog registerDialog = new AlertDialog.Builder(LoginActivity.this).setView(registerView).setMessage(R.string.registerdialogtxt).setPositiveButton(R.string.ok, (dialog, which) ->{
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

        AlertDialog loginDialog = new AlertDialog.Builder(LoginActivity.this).setView(loginView).setMessage(R.string.logindialogtxt).setPositiveButton(R.string.ok, (dialog, which) ->{
            logUser(loginBinder.emailSet.getText().toString(), loginBinder.passwSet.getText().toString());
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


}
