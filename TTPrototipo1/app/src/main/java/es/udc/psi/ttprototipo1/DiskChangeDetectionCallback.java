package es.udc.psi.ttprototipo1;

public interface DiskChangeDetectionCallback {
    void onCangeDetected(int x, int y, int vx, int vy);
    void onFail(String errorMsg);
}
