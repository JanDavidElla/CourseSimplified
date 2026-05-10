package coursesimplified.model;

/**
 * Persisted user account metadata for login, password verification, and the
 * last major that user opened in the planner.
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
