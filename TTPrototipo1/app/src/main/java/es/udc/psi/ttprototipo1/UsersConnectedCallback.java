package es.udc.psi.ttprototipo1;

import java.util.ArrayList;

public interface UsersConnectedCallback {
    public void onUsersLoaded(ArrayList<User> users);
    public void onError(String errorMsg);
}

