package es.udc.psi.ttprototipo1;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;

public interface ChangesListenCallback{
    public void onManageChanges(String sender, String message);
    public void onError(String message);
}
