package es.udc.psi.ttprototipo1;

public interface DoNotDisturbCallback {
    void onSuccess(boolean policy);
    void onFailure(String errorMsg);
}
