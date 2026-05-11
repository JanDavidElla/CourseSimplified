package coursesimplified.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import coursesimplified.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manages account registration, login state, and user-scoped preferences such
 * as the last major loaded in the planner.
 */
public class UserService {
    private static final Type USER_MAP_TYPE = new TypeToken<Map<String, User>>() { }.getType();

    private final Path filePath;
    private final Gson gson;
    private final Map<String, User> usersById;
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private User currentUser;

    public UserService(Path filePath) {
        this(filePath, new Gson());
    }

    public UserService(Path filePath, Gson gson) {
        this.filePath = filePath;
        this.gson = gson;
        this.usersById = new LinkedHashMap<>();
        load();
    }

    public User login(String username, String password) {
        String normalizedUserId = normalizeUsername(username);
        validateCredentials(normalizedUserId, password);

        User user = usersById.get(normalizedUserId);
        if (user == null) {
            throw new IllegalArgumentException("No account exists for that username.");
        }
        if (!passwordHasher.verification(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        currentUser = user;
        return user;
    }

    public User register(String username, String password) {
        String normalizedUserId = normalizeUsername(username);
        validateCredentials(normalizedUserId, password);

        if (usersById.containsKey(normalizedUserId)) {
            throw new IllegalArgumentException("That username already exists.");
        }

        User user = new User(normalizedUserId, username.trim(), passwordHasher.storableHashObject(password));
        usersById.put(normalizedUserId, user);
        save();
        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean hasCurrentUser() {
        return currentUser != null;
    }

    public String getCurrentUserId() {
        return currentUser == null ? "" : currentUser.getUserId();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getLastMajorForCurrent() {
        return currentUser == null ? null : currentUser.getLastMajor();
    }

    public void setLastMajorForCurrent(String majorName) {
        if (currentUser == null) {
            return;
        }
        currentUser.setLastMajor(majorName);
        save();
    }

    private void validateCredentials(String normalizedUserId, String password) {
        if (normalizedUserId.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
    }

    private void load() {
        if (!Files.exists(filePath)) {
            return;
        }

        try {
            String json = Files.readString(filePath);
            Map<String, User> loadedUsers = gson.fromJson(json, USER_MAP_TYPE);
            if (loadedUsers != null) {
                usersById.putAll(loadedUsers);
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Warning: could not read " + filePath + ": " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.writeString(filePath, gson.toJson(usersById));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save users: " + e.getMessage(), e);
        }
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
