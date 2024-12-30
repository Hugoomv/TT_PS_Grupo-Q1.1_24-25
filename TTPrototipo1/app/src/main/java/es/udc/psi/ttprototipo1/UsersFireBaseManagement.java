package es.udc.psi.ttprototipo1;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UsersFireBaseManagement {

    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();

    private static UsersFireBaseManagement instance;

    private UsersFireBaseManagement(){}

    public static UsersFireBaseManagement getInstance() {
        if (instance == null) {
            instance = new UsersFireBaseManagement();
        }
        return instance;
    }

    public void loginUser(String email, String password, FirebaseAuth mAuth, Activity actividad, UserLoginCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(actividad, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        callback.onSuccess();
                    } else {
                        // Si falla el inicio de sesión, muestra un mensaje
                        callback.onFailure(task.getException().getMessage());
                    }
                }
            });
    }

    public void registerUser(String name, String email, String passw, FirebaseAuth mAuth, UserSetupCallback callback){
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
                                    callback.onSuccessfulTask();
                                }else{
                                    callback.onFailedTask("registro fallido " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                }else{
                    callback.onFailedTask("registro fallido " + task.getException().getMessage());
                }
            }
        });
    }

    public void logoutUser(FirebaseUser user){
        rtFireBaseManagement.updateUserConnectionStatus(user, false);
        rtFireBaseManagement.stopListeningToChanges();
        FirebaseAuth.getInstance().signOut();
    }

    public void deleteUser(FirebaseUser userToDelete, UserDeleteCallback callback){

        //borramos los datos del usuario en RealTime Database

        rtFireBaseManagement.updateUserConnectionStatus(userToDelete, false);
        rtFireBaseManagement.stopListeningToChanges();

        rtFireBaseManagement.deleteUserInDatabase(userToDelete, new UserDeleteCallback() {
            @Override
            public void onSuccessfulRemove() {
                //borramos el usuario
                userToDelete.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        callback.onSuccessfulRemove();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Error", e.getMessage());
                    }
                });
            }
        });
    }


}
