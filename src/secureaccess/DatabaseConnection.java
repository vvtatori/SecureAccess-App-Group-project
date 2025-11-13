/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

/**
 *
 * @author Linru
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:password_manager.db"; 

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            System.out.println(" Connection successful!");
        } catch (SQLException e) {
            System.out.println(" Connection failed: " + e.getMessage());
        }
        return conn;
    }

    // Test it
    public static void main(String[] args) {
        connect();
    }
}
