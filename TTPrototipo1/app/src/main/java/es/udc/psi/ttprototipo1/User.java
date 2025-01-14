package es.udc.psi.ttprototipo1;

public class User {
    private String userId;
    private String name;
    private String email;
    private boolean isConnected;
    private long lastSeen;
    private int matchesPlayed, matchesWon;

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

    public User(String name,  String email, int played, int won){
        this.name = name;
        this.email = email;
        matchesPlayed = played;
        matchesWon = won;
    }

    // Getters y setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isConnected() { return isConnected; }
    public long getLastSeen() { return lastSeen; }
    public int getMatchesPlayed() { return matchesPlayed; }
    public int getMatchesWon(){ return matchesWon; }
}
