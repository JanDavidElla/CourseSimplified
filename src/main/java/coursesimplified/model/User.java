package coursesimplified.model;

/**
 * A user account with username, password hash, and which major they last viewed.
 */
public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String lastMajor;

    public User() {
    }

    public User(String userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getLastMajor() {
        return lastMajor;
    }

    public void setLastMajor(String lastMajor) {
        this.lastMajor = lastMajor;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
