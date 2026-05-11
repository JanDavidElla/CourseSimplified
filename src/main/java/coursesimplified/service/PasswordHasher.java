package coursesimplified.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashes and verifies user passwords with PBKDF2 and a per-password salt.
 */
public class PasswordHasher {
    /**
     * Returns a storable "salt:hash" string where both values are Base64
     * encoded.
     */
    public String storableHashObject(String plainTextPassword) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(plainTextPassword.toCharArray(), salt, 310_000, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = secretKeyFactory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt)
                    + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash password.", e);
        }
    }

    public boolean verification(String enteredPassword, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Stored password hash is invalid.");
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        try {
            PBEKeySpec spec = new PBEKeySpec(enteredPassword.toCharArray(), salt, 310_000, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actualHash = secretKeyFactory.generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(actualHash, expectedHash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify password.", e);
        }
    }
}
