package coursesimplified.service;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TransferQueue;


public class PasswordHasher {
    public PasswordHasher() {}
    //generate a new users hash object to be stored in the user json file. The format is salt:hash, both base64 encoded.
    public String storableHashObject(String plainTextPassword){
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(plainTextPassword.toCharArray(), salt, 65536, 128);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    //compare guess to password hash using the base64 entry.
    public boolean verification(String formPassword, String base64hash){

        String[] parts = base64hash.split(":");
        String saltBase64 = parts[0];
        String hashBase64 = parts[1];

        byte[] hash = Base64.getDecoder().decode(hashBase64);
        byte[] salt = Base64.getDecoder().decode(saltBase64);

        byte[] guessHash;
        try {
            PBEKeySpec spec = new PBEKeySpec(formPassword.toCharArray(), salt, 65536, 128);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            guessHash = skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing guess password", e);
        }
        //moire secure than Arrays.equals for comparing the hashes.
        return MessageDigest.isEqual(guessHash, hash);
    }
}
