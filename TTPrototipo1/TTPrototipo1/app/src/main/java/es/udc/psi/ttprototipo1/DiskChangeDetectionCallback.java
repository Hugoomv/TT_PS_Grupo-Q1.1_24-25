package es.udc.psi.ttprototipo1;

public interface DiskChangeDetectionCallback {
    void onCangeDetected(float x, float y, float vx, float vy);
    void onFail(String errorMsg);
}
