package es.udc.psi.ttprototipo1;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RTFireBaseManagement {

    private DatabaseReference myData;
    private ValueEventListener myDataListener;

    private DatabaseReference myMatchData;
    private ValueEventListener myMatchDataListener;

    private DatabaseReference playerScore;
    private ValueEventListener controlPScore;

    private DatabaseReference whoWins;
    private ValueEventListener winnerListener;

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
                    callback.onSuccessfulTask();
                } else {
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
                callback.onSuccessfulRemove();
            }
        });
    }

    public void changeDoNotDisturb(String userId, boolean disturb, DoNotDisturbCallback callback){

        FirebaseDatabase.getInstance().getReference("users").child(userId).child("isAvailable").setValue(disturb).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    callback.onSuccess(disturb);
                }else{
                    callback.onFailure(task.getException().getMessage());
                }
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
        player1Data.put("score", 5);
        player1Data.put("role", "bottom");

        Map<String, Object> player2Data = new HashMap<>();
        player2Data.put("id", idUs2);
        player2Data.put("score", 5);
        player2Data.put("role", "top");

        Map<String, Object> diskData = new HashMap<>();
        diskData.put("x", 200.0);
        diskData.put("y", 200.0);
        diskData.put("vx", 5.0);
        diskData.put("vy", -5.0);

        Map<String, Object> matchData = new HashMap<>();
        matchData.put("player1", player1Data);
        matchData.put("player2", player2Data);
        matchData.put("disk", diskData);
        matchData.put("diskOwner", "player1");
        matchData.put("status", "inProgress");
        matchData.put("winner", "");

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

    public void deleteMatch(String matchId, MatchDeleteCallback callback){

        if (matchId == null || matchId.isEmpty()) {
            return;
        }

        DatabaseReference dataToDelete = FirebaseDatabase.getInstance().getReference("matches").child(matchId);

        dataToDelete.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void changeDisk(String partidaId, float x, float y, float vx, float vy){
        DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("matches").child(partidaId);
        matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                matchRef.child("disk").child("x").setValue(x);
                matchRef.child("disk").child("y").setValue(y);
                matchRef.child("disk").child("vx").setValue(vx);
                matchRef.child("disk").child("vy").setValue(vy);

                String oldOwner = snapshot.child("diskOwner").getValue(String.class);
                if(oldOwner.equals("player1")){
                    matchRef.child("diskOwner").setValue("player2");
                }else if(oldOwner.equals("player2")){
                    matchRef.child("diskOwner").setValue("player1");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", "no se pudo cambiar la pelota");
            }
        });
    }

    public void updateScore(String playerId, String partidaId, int score){
        DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("matches").child(partidaId);
        matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("player1").child("id").getValue(String.class).equals(playerId)){
                    matchRef.child("player1").child("score").setValue(score);
                }else{
                    matchRef.child("player2").child("score").setValue(score);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", "no se pudo actualizar la puntuacion");
            }
        });
    }

    public void detectDiskChanges(String playerId, String partidaId, DiskChangeDetectionCallback callback){
        /*Añadimos listener para quien tiene la bola
        * devolvemos con un callback las coordenadas del disco*/
        myMatchData = FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("diskOwner");
        myMatchDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference matchRef = FirebaseDatabase.getInstance().getReference("matches").child(partidaId);
                matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        float x = snapshot.child("disk").child("x").getValue(Float.class);
                        float y = snapshot.child("disk").child("y").getValue(Float.class);
                        float vx = snapshot.child("disk").child("vx").getValue(Float.class);
                        float vy = snapshot.child("disk").child("vy").getValue(Float.class);

                        String newOwner = snapshot.child("diskOwner").getValue(String.class);
                        String newOwnerId = snapshot.child(newOwner).child("id").getValue(String.class);

                        if(newOwnerId.equals(playerId)){
                            callback.onCangeDetected(x, y, vx, vy);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFail(error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFail(error.getMessage());
            }
        };
        myMatchData.addValueEventListener(myMatchDataListener);
    }

    public void stopDetectingDiskChanges(){
        //Borramos el listener
        if(myMatchData != null && myMatchDataListener != null){
            myMatchData.removeEventListener(myMatchDataListener);
            myMatchData = null;
            myMatchDataListener = null;
        }
    }

    public void controlScore(String partidaId, String playerId){
        FirebaseDatabase.getInstance().getReference("matches").child(partidaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean is1;

                if(snapshot.child("player1").child("id").getValue(String.class).equals(playerId)){

                    is1 = true;
                    playerScore = FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("player1").child("score");
                }else{
                    is1 = false;
                    playerScore = FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("player2").child("score");
                }

                controlPScore = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot newSnapshot) {
                        String winner;

                        if(newSnapshot.getValue(Integer.class) <= 0){
                            if(is1){
                                winner = snapshot.child("player2").child("id").getValue(String.class);
                                FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("winner").setValue(winner);

                            }else{
                                winner = snapshot.child("player1").child("id").getValue(String.class);
                                FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("winner").setValue(winner);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        return;
                    }
                };

                playerScore.addValueEventListener(controlPScore);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });

    }

    public void forfeit(String partidaId, String playerId){

        FirebaseDatabase.getInstance().getReference("matches").child(partidaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child("player1").child("id").getValue(String.class).equals(playerId)){

                    FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("winner").setValue(snapshot.child("player2").child("id").getValue(String.class));
                }else{
                    FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("winner").setValue(snapshot.child("player1").child("id").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.d("Error", error.getMessage());
            }
        });
    }

    public void stopControlingScore(){
        if(playerScore != null && controlPScore != null){
            playerScore.removeEventListener(controlPScore);
            playerScore = null;
            controlPScore = null;
        }
    }

    public void controlWinner(String partidaId, WinnerCallback callback){

        whoWins = FirebaseDatabase.getInstance().getReference("matches").child(partidaId).child("winner");
        winnerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String winner = snapshot.getValue(String.class);
                if(!winner.equals("")){
                    callback.declareWinner(winner);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", error.getMessage());
            }
        };
        whoWins.addValueEventListener(winnerListener);
    }

    public void stopControlingWinner(){
        if(whoWins != null && winnerListener != null){
            whoWins.removeEventListener(winnerListener);
            whoWins = null;
            winnerListener = null;
        }
    }

    public void setUpUserInRankingDatabase(FirebaseUser newUser, UserSetupCallback callback){
        String idUs = newUser.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("ranking").child(idUs);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", newUser.getDisplayName());
        userData.put("uId", idUs);
        userData.put("matchesPlayed", 0);
        userData.put("matchesWon", 0);

        userRef.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccessfulTask();
                } else {
                    callback.onFailedTask("registro fallido " + task.getException().getMessage());
                }
            }
        });
    }

    public void updateRankingStats(FirebaseUser user, boolean won){
        FirebaseDatabase.getInstance().getReference("ranking").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int matchPlayed, matchWon;
                matchPlayed = snapshot.child("matchesPlayed").getValue(Integer.class);
                matchWon = snapshot.child("matchesWon").getValue(Integer.class);
                FirebaseDatabase.getInstance().getReference("ranking").child(user.getUid()).child("matchesPlayed").setValue(matchPlayed+1);
                if(won){
                    FirebaseDatabase.getInstance().getReference("ranking").child(user.getUid()).child("matchesWon").setValue(matchWon+1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Error", error.getMessage());
            }
        });

    }

    public void topTen(FirebaseUser currentUser, UsersConnectedCallback callback){
        ArrayList<User> totalUsers = new ArrayList<>();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("ranking");

        userRef.orderByChild("matchesWon").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                totalUsers.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    String name = userSnapshot.child("name").getValue(String.class);
                    String uid = userSnapshot.child("uId").getValue(String.class);
                    int matchesPlayed = userSnapshot.child("matchesPlayed").getValue(Integer.class);
                    int matchesWon = userSnapshot.child("matchesWon").getValue(Integer.class);

                    if(name != null) {
                        User user = new User(name, matchesPlayed, matchesWon);
                        totalUsers.add(user);
                    }
                }

                Collections.reverse(totalUsers);
                callback.onUsersLoaded(totalUsers);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void deleteUserInRanking(FirebaseUser userToDelete, UserDeleteCallback callback){
        DatabaseReference dataToDelete = FirebaseDatabase.getInstance().getReference("ranking").child(userToDelete.getUid());

        dataToDelete.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                callback.onSuccessfulRemove();
            }
        });
    }

}
