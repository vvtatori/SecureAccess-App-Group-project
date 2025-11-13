/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

/**
 *
 * @author shaun
 */
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hashing {
//    public static String hashPassword(char[] password) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] bytes = new String(password).getBytes();  //bytes[] converts the string into bytes so the password hashing will work
//            byte[] hashed = md.digest(bytes);
//
//            StringBuilder sb = new StringBuilder();
//            for (byte b : hashed) {
//                sb.append(String.format("%02x", b));
//            }
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public static String generateOTP(int length) {
        String digits = "0123456789";
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(digits.charAt(random.nextInt(digits.length())));
        }
        return otp.toString();
    }
    
    //Lin's code
    private Hashing() {
    }

    public static final int ITERATIONS = 100_000;
    private static final int SALT_BYTES = 16;   // 16 bytes
    private static final int KEY_BITS = 256;  // 32 bytes

    public static String generateSaltBase64() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

//    public static String hashPassword(char[] password, String saltBase64) {
//        try {
//            byte[] salt = Base64.getDecoder().decode(saltBase64);
//            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
//            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            byte[] hash = f.generateSecret(spec).getEncoded();
//            return Base64.getEncoder().encodeToString(hash);
//        } catch (Exception e) {
//            throw new IllegalStateException("PBKDF2 failed", e);
//        } finally {
//            java.util.Arrays.fill(password, '\0');
//        }
//    }
    public static String hashPassword(char[] password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = f.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            // Log the error and throw an unchecked exception, as failed hashing is fatal.
            // Ensure the password array is cleared even if an exception occurs
            Arrays.fill(password, '\0'); 
            throw new IllegalStateException("PBKDF2 failed during password hashing", e);
        } finally {
            // Securely wipe the input password array from memory
            Arrays.fill(password, '\0'); 
        }
    }


//    public static boolean verify(char[] candidate, String saltBase64, String expectedHashBase64) {
//        String cand = hashPassword(candidate, saltBase64);
//        byte[] a = Base64.getDecoder().decode(cand);
//        byte[] b = Base64.getDecoder().decode(expectedHashBase64);
//        if (a.length != b.length) {
//            return false;
//        }
//        int r = 0;
//        for (int i = 0; i < a.length; i++) {
//            r |= a[i] ^ b[i];
//        }
//        return r == 0;
//    }
    /**
     * Verifies a candidate password against an expected hash using the same salt.
     */
    public static boolean verify(char[] candidate, String saltBase64, String expectedHashBase64) {
        // We call hashPassword, which handles the cleanup of the candidate array.
        String candidateHashBase64 = hashPassword(candidate, saltBase64);
        
        // Use time-constant comparison to prevent timing attacks
        return MessageDigest.isEqual(
            Base64.getDecoder().decode(candidateHashBase64),
            Base64.getDecoder().decode(expectedHashBase64)
        );
    }
}

