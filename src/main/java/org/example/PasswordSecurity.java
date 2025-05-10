package org.example;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordSecurity {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_BYTES_NUMBER=16;
    public static byte[] getNewSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES_NUMBER];
        random.nextBytes(salt);
        return salt;
    }
    public static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt);
            byte[] encodedHash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("at 'hashPassword': " + e.getMessage(), e);
        }
    }

}
