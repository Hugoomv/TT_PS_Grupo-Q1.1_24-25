package es.udc.psi.ttprototipo1;

public class User {
    private String userId;
    private String name;
    private String email;
    private boolean isConnected;
    private long lastSeen;

    public User() {
        // Constructor vacío necesario para Firebase
    }

    public User(String userId, String name, String email, boolean isConnected, long lastSeen) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.isConnected = isConnected;
        this.lastSeen = lastSeen;
    }

    // Getters y setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isConnected() { return isConnected; }
    public long getLastSeen() { return lastSeen; }
}
