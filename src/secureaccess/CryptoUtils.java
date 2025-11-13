/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.nio.ByteBuffer;
/**
 *
 * @author shemeneroje
 */
public class CryptoUtils {
    private static final int IV_SIZE = 12;      // recommended for GCM
    private static final int TAG_BITS = 128;    // 128-bit tag

    public static byte[] generateKey() {
        byte[] key = new byte[32]; // 256-bit
        new SecureRandom().nextBytes(key);
        return key;
    }

    public static String encrypt(String plain, byte[] key) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec k = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, k, new GCMParameterSpec(TAG_BITS, iv));
        byte[] ct = cipher.doFinal(plain.getBytes("UTF-8"));

        ByteBuffer bb = ByteBuffer.allocate(iv.length + ct.length);
        bb.put(iv).put(ct);
        return Base64.getEncoder().encodeToString(bb.array());
    }

    public static String decrypt(String base64Blob, byte[] key) throws Exception {
        byte[] blob = Base64.getDecoder().decode(base64Blob);
        ByteBuffer bb = ByteBuffer.wrap(blob);

        byte[] iv = new byte[IV_SIZE];
        bb.get(iv);
        byte[] ct = new byte[bb.remaining()];
        bb.get(ct);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec k = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, k, new GCMParameterSpec(TAG_BITS, iv));
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, "UTF-8");
    }
    
    //@author Virginiah
    /**
     * Generates a cryptographically strong, random password.
     * @param length The desired length of the password.
     * returns A strong, random password string.
     */
    public static String generateStrongPassword(int length) {
        final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final String SYMBOLS = "!@#$%^&*()-_+=<>?";
        final String ALL_CHARS = UPPER + LOWER + DIGITS + SYMBOLS;
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each set for a strong password
        if (length >= 4) {
            password.append(UPPER.charAt(random.nextInt(UPPER.length())));
            password.append(LOWER.charAt(random.nextInt(LOWER.length())));
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
            password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }

        // Fill the rest of the length with random characters from all sets
        for (int i = password.length(); i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the characters to randomize the position of the mandatory characters
        for (int i = 0; i < length; i++) {
            int randomPosition = random.nextInt(length);
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomPosition));
            password.setCharAt(randomPosition, temp);
        }

        return password.toString();
    }
}