package es.udc.psi.ttprototipo1;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

public interface ChangesListenCallback{
    void onManageChanges(String sender, String senderId, String message);
    void onError(String message);
}
