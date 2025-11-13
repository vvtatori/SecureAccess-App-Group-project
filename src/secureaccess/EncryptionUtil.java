/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author vvtat
 */

/**
 * This acts as the main interface for the application's encyption and decryption operations. It handles loading the AES key
 * via KeyManager and delegates cryptographic operations to CryptoUtils.
 */
public class EncryptionUtil {
    private static byte[] AES_KEY = null;

    // a Static initializer to ensure the key is loaded or created once
    // when the class is first accessed.
    static {
        try {
            AES_KEY = KeyManager.loadOrCreateKey();
            System.out.println("AES Key loaded/created successfully.");
            // Print key size for confirmation (should be 32 bytes for AES-256)
            // System.out.println("Key size: " + AES_KEY.length + " bytes."); 
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "FATAL ERROR: Could not load or create the encryption key file.", 
                "Security Initialization Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            // In a serious application, we might halt here: System.exit(1);
        }
    }

    /**
     * Encrypts a plaintext password using the application's master AES key.
     * The result is a Base64-encoded string containing the IV and ciphertext.
     * * @param plainText The password to encrypt.
     * returns The Base64-encoded encrypted string, or null on failure.
     */
    public static String encrypt(String plainText) {
        if (AES_KEY == null) {
            System.err.println("Cannot encrypt: AES Key is not initialized.");
            return null;
        }
        try {
            return CryptoUtils.encrypt(plainText, AES_KEY);
        } catch (Exception e) {
            System.err.println("Encryption failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts a Base64-encoded encrypted password blob using the application's 
     * master AES key.
     * * @param base64Blob The Base64-encoded string containing IV and ciphertext.
     * returns The decrypted plaintext password, or null on failure.
     */
    public static String decrypt(String base64Blob) {
        if (AES_KEY == null) {
            System.err.println("Cannot decrypt: AES Key is not initialized.");
            return null;
        }
        if (base64Blob == null || base64Blob.isEmpty()) {
            return ""; // Handle null/empty input gracefully
        }
        try {
            return CryptoUtils.decrypt(base64Blob, AES_KEY);
        } catch (Exception e) {
            System.err.println("Decryption failed (Possible Tampering or Corrupted Data): " + e.getMessage());
            e.printStackTrace();
            // Return a safe message instead of crashing or returning bad data
            return "[Decryption Failed]"; 
        }
    }
    
    // Method to securely hash the master passwords for user login authentication
    // relies on the  Hashing.java for authentication
//    public static String hashMasterPassword(char[] password) {
//        return Hashing.hashPassword(password);
//    }
    
    //Public static getter method to safely retrieve the key
    /**
     * Retrieves the application's master AES key.
     * returns The AES key byte array, or null if initialization failed.
     */
    public static byte[] getAESKey() {
        return AES_KEY;
    }
    
    //Securely wipes the AES key from memory. Will be called on application exit/logout.
    public static void wipeKey() {
        if (AES_KEY != null) {
            // Overwrite the array contents with zeros to securely wipe it from memory
            java.util.Arrays.fill(AES_KEY, (byte) 0);
            AES_KEY = null;
        }
    }
    
}
