package es.udc.psi.ttprototipo1;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RTFireBaseManagement {

    private DatabaseReference myData;
    private ValueEventListener myDataListener;

    private static RTFireBaseManagement instance;

    private RTFireBaseManagement() {}

    public static RTFireBaseManagement getInstance() {
        if (instance == null) {
            instance = new RTFireBaseManagement();
        }
        return instance;
    }

    public void setUpUserInDatabase(FirebaseUser newUser, UserSetupCallback callback){
        String name = newUser.getDisplayName();
        String email = newUser.getEmail();
        String idUs = newUser.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(idUs);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", idUs);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("isConnected", true);
        userData.put("isAvailable", true);
        userData.put("lastSeen", ServerValue.TIMESTAMP);
        userData.put("isWith", "");
        userData.put("message", "");

        userRef.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(contexto, "Usuario guardado en la bd", Toast.LENGTH_SHORT).show();
                    callback.onSuccessfulTask();
                } else {
                    //Toast.makeText(contexto, "Error al guardar usuario en la bd", Toast.LENGTH_SHORT).show();
                    callback.onFailedTask("registro fallido " + task.getException().getMessage());
                }
            }
        });
    }

    public void deleteUserInDatabase(FirebaseUser userToDelete, UserDeleteCallback callback){
        DatabaseReference dataToDelete = FirebaseDatabase.getInstance().getReference("users").child(userToDelete.getUid());

        dataToDelete.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Toast.makeText(contexto, "Usuario borrado de la bd", Toast.LENGTH_SHORT).show();
                callback.onSuccessfulRemove();
            }
        });
    }

    public void updateUserConnectionStatus(FirebaseUser user, boolean isConnected) {
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("isConnected").setValue(isConnected);  // Actualizar el estado de conexión
        userRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);  // Actualizar el último tiempo de conexión

        // Configurar onDisconnect para manejar desconexión inesperada
        if (isConnected) {
            userRef.child("isConnected").onDisconnect().setValue(false);
            userRef.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP);
        }
    }

    public void connectedUsers(FirebaseUser currentUser, UsersConnectedCallback callback){
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
                        if(!userId.equals(currentUser.getUid())) {
                            User user = new User(userId, name, email, isConnected, lastSeen);
                            totalUsers.add(user);
                        }
                    }
                }
                callback.onUsersLoaded(totalUsers);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void stopListeningToChanges(){
        if(myDataListener != null && myData != null){
            myData.removeEventListener(myDataListener);
            myData = null;
            myDataListener = null;
        }
    }

    public void listenToChanges(ChangesListenCallback callback){

        stopListeningToChanges();

        //estar atentos a solicitudes
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            myData = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            myDataListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child("isWith").getValue(String.class).isEmpty()) {
                        String remitente = snapshot.child("isWith").getValue(String.class);
                        DatabaseReference senderData = FirebaseDatabase.getInstance().getReference("users").child(remitente);

                        senderData.child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot anotherSnapshot) {
                                //para recepción de mensajes
                                String sender = anotherSnapshot.getValue(String.class);
                                String message = snapshot.child("message").getValue(String.class);
                                callback.onManageChanges(sender, message, snapshot);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //para errores en la recepción de mensajes
                                callback.onError(error.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //para errores en la recepción de mensajes
                    callback.onError(error.getMessage());
                }
            };
            myData.addValueEventListener(myDataListener);
        }
    }
}
