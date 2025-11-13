/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.Base64;
/**
 *
 * @author shemeneroje
 */
public class SessionManager {
    private IdleMonitor monitor;
    private String sessionToken;
    private final byte[] aesKey;

    public SessionManager(byte[] aesKey) {
        this.aesKey = aesKey;
        SessionDAO.createTableIfMissing();
    }

    public String startSession(String username, long timeoutMs, long warnMs) {
        stop();
        sessionToken = generateToken();
        SessionDAO.create(sessionToken, username, timeoutMs); 
        monitor = new IdleMonitor(timeoutMs, warnMs, this::handleTimeout);
        return sessionToken;
    }

    public void touch() {
        if (sessionToken != null) SessionDAO.touch(sessionToken);
        if (monitor != null) monitor.restart();
    }

    public void stop() {
        if (monitor != null) {
            monitor.stop();
            monitor = null;
        }
        if (sessionToken != null) {
            SessionDAO.delete(sessionToken);
            sessionToken = null;
        }
    }

    private void handleTimeout() {
        stop();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Session expired due to inactivity.");
            new loginGUI().setVisible(true);
        });
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
