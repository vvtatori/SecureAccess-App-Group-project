/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.time.LocalDateTime;
/**
 *
 * @author vvtat
 */

public class DBhelper {
    private static final String URL = "jdbc:sqlite:password_manager.db";

    // Connect to the database (//Removing this and using the connection in the DatabaseConnection File.) @Virginiah
//    public static Connection connect() {
//        Connection conn = null;
//        try {
//            //trying to connect to the database
//            conn = (Connection) DriverManager.getConnection(URL);  //getconnection gives an object that lets code interact with the database
//            System.out.println("Connection successful!");
//        } catch (SQLException e) {  
//            System.out.println("Connection failed: " + e.getMessage());
//        }
//        return conn;
//    }

    
    // create users table
//    public static void initializeUsersTable() {
//        String sql = "CREATE TABLE IF NOT EXISTS users (" 
//                   + "id INTEGER PRIMARY KEY AUTOINCREMENT," //autoincrement makes sure every input has a unique ID
//                   + "name TEXT NOT NULL,"  //name cant be empty
//                   + "email TEXT NOT NULL UNIQUE," //email must be unique to the user
//                   + "password TEXT NOT NULL" //hashed password cant be empty
//                   + ");";
//        //try catch automatically closes database connection once the try block ends, even if there is an error
//        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
//            stmt.execute(sql);
//            System.out.println("Users table created or already exists.");
//        } catch (SQLException e) {
//            System.out.println("Error creating table: " + e.getMessage());
//        }
//    }
    
    public static void initializeUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" 
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL,"  
                    + "email TEXT NOT NULL UNIQUE," 
                    + "password_hash TEXT NOT NULL," // New security field
                    + "password_salt TEXT NOT NULL," // New security field
                    + "two_factor_enabled INTEGER DEFAULT 0,"
                    + "account_locked INTEGER DEFAULT 0,"
                    + "last_login TEXT,"
                    + "created_at TEXT NOT NULL,"
                    + "updated_at TEXT"
                    + ");";
        
        // Corrected: Uses DatabaseConnection.connect() to get the connection @Virginiah
        // try-with-resources ensures the connection and statement are closed automatically.
        try (Connection conn = DatabaseConnection.connect(); 
            Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("Users table created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    // saving users
//    public static boolean saveUser(String name, String email, String hashedPass) {
//        //the ??? are placeholders for where the name, email, and password are going
//        String sql = "INSERT INTO users(name,email,password) VALUES (?, ?, ?)";
//        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, name); //name is inserted where the first ? is
//            pstmt.setString(2, email);
//            pstmt.setString(3, hashedPass);
//            pstmt.executeUpdate();  
//            return true;
//        } catch (SQLException e) {
//            System.out.println("Error saving user: " + e.getMessage());
//            return false;
//        }
//    }
    
    public static boolean saveUser(String name, String email, String hashedPassword, String saltBase64) {
        // SQL query with placeholders for security
        String sql = "INSERT INTO users(username, email, password_hash, password_salt, created_at) " 
               + "VALUES (?, ?, ?, ?, ?)"; 
        
        String timestamp = LocalDateTime.now().toString();
        
        // Corrected: Uses DatabaseConnection.connect() to get the connection  @Virginiah
        try (Connection conn = DatabaseConnection.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);             // 1: username
            pstmt.setString(2, email);            // 2: email
            pstmt.setString(3, hashedPassword);   // 3: password_hash
            pstmt.setString(4, saltBase64);       // 4: password_salt
            pstmt.setString(5, timestamp);        // 5: created_at 
            
            pstmt.executeUpdate();  
            return true;
        } catch (SQLException e) {
            // Print error, particularly for UNIQUE constraint violation on email
            System.out.println("Error saving user: " + e.getMessage());
            return false;
        }
    }
    
    
    //@Author: Virginiah
    //Retrieves the hashed password for a given email address.
//    public static String getHashedPasswordByEmail(String email) {
//        String sql = "SELECT password FROM users WHERE email = ?";
//        
//        try (Connection conn = DatabaseConnection.connect(); 
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            
//            pstmt.setString(1, email);
//            ResultSet rs = pstmt.executeQuery();
//            
//            if (rs.next()) {
//                return rs.getString("password");
//            }
//        } catch (SQLException e) {
//            System.out.println("Error retrieving user: " + e.getMessage());
//        }
//        return null;
//    }
    
    /** Retrieves the hashed password and salt for login verification. */
    public static String[] getHashedPasswordAndSaltByEmail(String email) {
        String sql = "SELECT password_hash, password_salt FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String hash = rs.getString("password_hash");
                String salt = rs.getString("password_salt");
                return new String[]{hash, salt};
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user credentials: " + e.getMessage());
        }
        return null;
    }
    
    //Retrieves user ID by email (Needed for linking passwords by ID).
    public static int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user ID: " + e.getMessage());
        }
        return -1; // Indicates user not found or error
    }
    
    //Creates the passwords table if it doesn't exist.
    public static void initializePasswordsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS passwords (" 
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                   + "user_id TEXT NOT NULL," // Storing user email as ID
                   + "service_name TEXT NOT NULL,"
                   + "service_username TEXT NOT NULL,"
                   + "url TEXT,"
                   + "category TEXT,"
                   + "notes TEXT,"
                   + "enc_password TEXT NOT NULL," // NEW: Renamed from 'password'
                   + "enc_salt TEXT,"              // NEW: If entry-specific salt is used
                   + "enc_iv TEXT,"                // NEW: Initialization Vector for AES
                   + "created_at TEXT NOT NULL,"
                   + "updated_at TEXT,"
                   + "FOREIGN KEY(user_id) REFERENCES users(email)"
                   + ");";
        try (Connection conn = DatabaseConnection.connect(); 
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Passwords table created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating passwords table: " + e.getMessage());
        }
    }

    //Saves a new password entry for a specific user.
    public static boolean savePassword(PasswordEntry entry) {
        String sql = "INSERT INTO passwords(user_id, service_name, service_username, url, category, notes, enc_password, created_at) " 
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // NOTE: enc_salt/enc_iv are omitted if bundled with enc_password

        String timestamp = LocalDateTime.now().toString();
        
        try (Connection conn = DatabaseConnection.connect(); 
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, entry.getUserEmail());
            pstmt.setString(2, entry.getName()); 
            pstmt.setString(3, entry.getUsername()); 
            pstmt.setString(4, entry.getUrl());
            pstmt.setString(5, entry.getCategory());
            pstmt.setString(6, entry.getNotes());
            pstmt.setString(7, entry.getEncryptedPassword()); // Assuming encrypted blob contains IV/Salt if needed
            pstmt.setString(8, timestamp); // created_at
            
            pstmt.executeUpdate();  
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving password: " + e.getMessage());
            return false;
        }
    }

    // Retrieves all password entries for a given user ID.
//    public static List<PasswordEntry> getAllPasswords(String userEmail) {
//        List<PasswordEntry> entries = new ArrayList<>();
//        // Updated SELECT query with the new column names
//        String sql = "SELECT id, service_name, service_username, url, notes, enc_password FROM passwords WHERE user_id = ?";
//        
//        try (Connection conn = DatabaseConnection.connect(); 
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            
//            pstmt.setString(1, userEmail);
//            ResultSet rs = pstmt.executeQuery();
//            
//            while (rs.next()) {
//                // Construct a PasswordEntry object from the database row
//                PasswordEntry entry = new PasswordEntry(
//                    rs.getInt("id"),
//                    userEmail, 
//                    rs.getString("service_name"),
//                    rs.getString("service_username"),
//                    rs.getString("enc_password"), // NEW COLUMN NAME
//                    rs.getString("url"),
//                    rs.getString("category"), 
//                    rs.getString("notes")
//                );
//                entries.add(entry);
//            }
//        } catch (SQLException e) {
//            System.out.println("Error retrieving passwords: " + e.getMessage());
//        }
//        return entries;
//    }
    
    public static List<PasswordEntry> getAllPasswords(String userEmail) {
        List<PasswordEntry> entries = new ArrayList<>();
        // FIX: Ensure the SELECT query asks for the correct columns, though the query was previously fixed.
        String sql = "SELECT id, service_name, service_username, url, notes, enc_password, category FROM passwords WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                // Construct a PasswordEntry object from the database row
                PasswordEntry entry = new PasswordEntry(
                    rs.getInt("id"),
                    userEmail, 
                    // FIX 1: Map 'service_name' to PasswordEntry.name
                    rs.getString("service_name"), 
                    // FIX 2: Map 'service_username' to PasswordEntry.username
                    rs.getString("service_username"), 
                    rs.getString("enc_password"),
                    rs.getString("url"),
                    rs.getString("category"), 
                    rs.getString("notes")
                );
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving passwords: " + e.getMessage());
        }
        return entries;
    }

    //Updates an existing password entry.
    public static boolean updatePassword(PasswordEntry entry) {
        String sql = "UPDATE passwords SET service_name = ?, service_username = ?, url = ?, category = ?, notes = ?, enc_password = ?, updated_at = ? WHERE id = ?";
        
        String timestamp = LocalDateTime.now().toString();

        try (Connection conn = DatabaseConnection.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // SET values (8 parameters)
            pstmt.setString(1, entry.getName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setString(3, entry.getUrl());
            pstmt.setString(4, entry.getCategory());
            pstmt.setString(5, entry.getNotes());
            pstmt.setString(6, entry.getEncryptedPassword()); // NEW COLUMN NAME
            pstmt.setString(7, timestamp); // updated_at
            
            // WHERE clause (Parameter 8)
            pstmt.setInt(8, entry.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    //Deletes a password entry by its ID.
    public static boolean deletePassword(int id, String userEmail) {
        //Check against 'user_id'
        String sql = "DELETE FROM passwords WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.setString(2, userEmail); // Security check: ensure only the owner can delete
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting password: " + e.getMessage());
            return false;
        }
    }

    // main method for testing database connection and table creation
    public static void main(String[] args) {
        DatabaseConnection.connect();
        initializeUsersTable();
    }
}

