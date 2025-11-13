/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author linru
 */
public final class CryptoUtil {
    private CryptoUtil() {}

    private static final int AES_KEY_BITS   = 256;  //32 bytes
    private static final int GCM_TAG_BITS   = 128;  //16 bytes tag
    private static final int GCM_IV_BYTES   = 12;   //12-byte nonce for GCM
    private static final int SALT_BYTES     = 16;
    private static final int ITERATIONS     = 100_000;
    
    public static final class EncryptionResult {
        public final String ciphertextBase64;
        public final String saltBase64;
        public final String ivBase64;
        public EncryptionResult(String ct, String salt, String iv) {
            this.ciphertextBase64 = ct; this.saltBase64 = salt; this.ivBase64 = iv;
        }
    }

    //encrypt a plaintext using master password
    public static EncryptionResult encryptPassword(char[] masterPassword, String plaintext) {
        byte[] iv = new byte[GCM_IV_BYTES];
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(iv);
        new SecureRandom().nextBytes(salt);

        try {
            SecretKeySpec key = deriveAesKey(masterPassword, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcm = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcm);
            byte[] ct = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return new EncryptionResult(
                Base64.getEncoder().encodeToString(ct),
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM encrypt failed", e);
        } finally {
            
        }
    }

    //decrypt
    public static String decryptPassword(char[] masterPassword, String saltBase64, String ivBase64, String ciphertextBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] ct = Base64.getDecoder().decode(ciphertextBase64);

            SecretKeySpec key = deriveAesKey(masterPassword, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcm = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcm);
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM decrypt failed", e);
        }
    }

    private static SecretKeySpec deriveAesKey(char[] masterPassword, byte[] salt) throws Exception {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword, salt, ITERATIONS, AES_KEY_BITS);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = f.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } finally {

        }
    }
}
