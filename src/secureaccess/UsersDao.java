/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects the GUI to the DB, use Hashing.java
 * @author linru
 */
public class UsersDao {

    private final Connection conn;

    public UsersDao(Connection conn) {
        this.conn = conn;
    }

    //inserting signup
    public int createUser(String username, String email, char[] password) throws SQLException {
        String salt = Hashing.generateSaltBase64();
        String hash = Hashing.hashPassword(password, salt);

        String sql = """
            INSERT INTO users (username, email, password_hash, password_salt, two_factor_enabled, account_locked, created_at, updated_at)
            VALUES (?, ?, ?, ?, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public boolean verifyLogin(String userOrEmail, char[] password) throws SQLException {
        String sql = "SELECT id, password_hash, password_salt FROM users WHERE username=? OR email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userOrEmail);
            ps.setString(2, userOrEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String hash = rs.getString("password_hash");
                String salt = rs.getString("password_salt");
                boolean ok = Hashing.verify(password, salt, hash);
                if (ok) {
                    updateLastLogin(rs.getInt("id"));
                }
                return ok;
            }
        }
    }

    private void updateLastLogin(int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET last_login=CURRENT_TIMESTAMP, updated_at=CURRENT_TIMESTAMP WHERE id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}

