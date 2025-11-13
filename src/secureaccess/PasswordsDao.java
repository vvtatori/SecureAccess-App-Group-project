/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author linru
 */

public class PasswordsDao {
    private final Connection conn;

    public PasswordsDao(Connection conn) { this.conn = conn; }

    // Save a new site password (AES-GCM encrypt using the master password the user just logged in with)
    public void addPassword(int userId, char[] masterPassword, String serviceName, String serviceUsername, String url, String plainPassword, String notes) throws SQLException {
        CryptoUtil.EncryptionResult er = CryptoUtil.encryptPassword(masterPassword, plainPassword);

        String sql = """
            INSERT INTO passwords (user_id, service_name, service_username, url, notes,
                                   enc_password, enc_salt, enc_iv, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, serviceName);
            ps.setString(3, serviceUsername);
            ps.setString(4, url);
            ps.setString(5, notes);
            ps.setString(6, er.ciphertextBase64);
            ps.setString(7, er.saltBase64);
            ps.setString(8, er.ivBase64);
            ps.executeUpdate();
        }

    }
    public void savePassword(int userId, char[] masterPassword, String service, String url, String plainPassword) throws SQLException {
    addPassword(userId, masterPassword, service, "", url, plainPassword, "");
}



    // List entries (without decrypting)
    public List<PasswordRow> listPasswords(int userId) throws SQLException {
        String sql = "SELECT password_id AS id, service_name, service_username, url, created_at FROM passwords WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<PasswordRow> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new PasswordRow(
                        rs.getInt("id"),
                        rs.getString("service_name"),
                        rs.getString("service_username"),
                        rs.getString("url"),
                        rs.getString("created_at")
                    ));
                }
                return out;
            }
        }
    }

    // Decrypt one entry to show/copy
    public String revealPassword(int entryId, char[] masterPassword) throws SQLException {
        String sql = "SELECT enc_password, enc_salt, enc_iv FROM passwords WHERE password_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, entryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return CryptoUtil.decryptPassword(
                    masterPassword,
                    rs.getString("enc_salt"),
                    rs.getString("enc_iv"),
                    rs.getString("enc_password")
                );
            }
        }
    }

    // Simple DTO
    public static class PasswordRow {
        public final int id; public final String serviceName, serviceUsername, url, createdAt;
        public PasswordRow(int id, String s, String u, String url, String c) {
            this.id = id; this.serviceName = s; this.serviceUsername = u; this.url = url; this.createdAt = c;
        }
    }
}
