package es.udc.psi.ttprototipo1;


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
    }

    public void connectedUsers(FirebaseUser currentUser, UsersConnectedCallback callback){
        ArrayList<User> totalUsers = new ArrayList<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.orderByChild("isConnected").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        String message = snapshot.child("message").getValue(String.class);

                        senderData.child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot anotherSnapshot) {
                                //para recepción de mensajes
                                String sender = anotherSnapshot.getValue(String.class);

                                callback.onManageChanges(sender, remitente, message);

                                snapshot.getRef().child("message").setValue("");
                                snapshot.getRef().child("isWith").setValue("");
                                snapshot.getRef().child("isAvailable").setValue(true);
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

    public void sendMessage(FirebaseUser sender, String userId, String message){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference neoUserRef = userRef.child(userId);
        neoUserRef.child("isWith").setValue(sender.getUid());
        neoUserRef.child("isAvailable").setValue(false);
        neoUserRef.child("message").setValue(message);
    }

    public void createMatch(String idUs1, String idUs2, MatchCreationCallback callback){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("matches").child(idUs1+idUs2);

        Map<String, Object> player1Data = new HashMap<>();
        player1Data.put("id", idUs1);
        player1Data.put("score", 0);
        player1Data.put("role", "bottom");

        Map<String, Object> player2Data = new HashMap<>();
        player1Data.put("id", idUs2);
        player1Data.put("score", 0);
        player1Data.put("role", "top");

        Map<String, Object> diskData = new HashMap<>();
        diskData.put("x", 540);
        diskData.put("y", 1700);
        diskData.put("vx", 0);
        diskData.put("vy", 0);

        Map<String, Object> matchData = new HashMap<>();
        matchData.put("player1", player1Data);
        matchData.put("player2", player2Data);
        matchData.put("diskOwner", player1Data);
        matchData.put("status", "inProgress");
        matchData.put("lastSeen", ServerValue.TIMESTAMP);

        userRef.setValue(matchData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFail(task.getException().getMessage());
                }
            }
        });
    }

    public void deleteMatch(String idUs1, String idUs2, MatchDeleteCallback callback){
        DatabaseReference dataToDelete = FirebaseDatabase.getInstance().getReference("matches").child(idUs1+idUs2);

        dataToDelete.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Toast.makeText(contexto, "Usuario borrado de la bd", Toast.LENGTH_SHORT).show();
                callback.onSuccessfulRemove();
            }
        });
    }
}
