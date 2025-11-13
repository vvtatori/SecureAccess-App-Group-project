/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/**
 *
 * @author shemeneroje
 */
public class SessionDAO {

    public static void createTableIfMissing() {
        String sql = "CREATE TABLE IF NOT EXISTS sessions ("
                   + " token TEXT PRIMARY KEY,"
                   + " username TEXT NOT NULL,"
                   + " last_activity_ms INTEGER NOT NULL,"
                   + " expires_at_ms INTEGER NOT NULL"
                   + ")";
        try (Connection c = DatabaseConnection.connect();
             Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean create(String token, String username, long ttlMs) {
        String sql = "INSERT INTO sessions(token, username, last_activity_ms, expires_at_ms) VALUES(?,?,?,?)";
        long now = System.currentTimeMillis();
        long exp = now + ttlMs;
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, username);
            ps.setLong(3, now);
            ps.setLong(4, exp);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void touch(String token) {
        String sql = "UPDATE sessions SET last_activity_ms = ? WHERE token = ?";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, token);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Optional<SessionRecord> get(String token) {
        String sql = "SELECT token, username, last_activity_ms, expires_at_ms FROM sessions WHERE token = ?";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SessionRecord rec = new SessionRecord(
                        rs.getString("token"),
                        rs.getString("username"),
                        rs.getLong("last_activity_ms"),
                        rs.getLong("expires_at_ms")
                    );
                    return Optional.of(rec);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    public static void delete(String token) {
        String sql = "DELETE FROM sessions WHERE token = ?";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class SessionRecord {
        public final String token;
        public final String username;
        public final long lastActivityMs;
        public final long expiresAtMs;

        public SessionRecord(String token, String username, long lastActivityMs, long expiresAtMs) {
            this.token = token;
            this.username = username;
            this.lastActivityMs = lastActivityMs;
            this.expiresAtMs = expiresAtMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expiresAtMs;
        }
        public long ageSeconds() {
            return (System.currentTimeMillis() - lastActivityMs) / 1000;
        }
        public long secondsUntilExpiry() {
            return Math.max(0, (expiresAtMs - System.currentTimeMillis()) / 1000);
        }
    }
}
