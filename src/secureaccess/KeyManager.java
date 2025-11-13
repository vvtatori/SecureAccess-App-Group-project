/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.nio.file.*;
import java.io.IOException;
/**
 *
 * @author shemeneroje
 */
public class KeyManager {
    private static final String KEY_FILE = "aes.key";

    public static byte[] loadOrCreateKey() throws IOException {
        Path p = Paths.get(KEY_FILE);
        if (Files.exists(p)) {
            byte[] key = Files.readAllBytes(p);
            if (key.length == 32) return key;
        }
        byte[] key = CryptoUtils.generateKey();
        Files.write(p, key);
        return key;
    }
}