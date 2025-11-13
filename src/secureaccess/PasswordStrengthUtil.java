/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureaccess;

/**
 *
 * @author vvtat
 */
public class PasswordStrengthUtil {
    /**
     * Calculates a password strength score (0-100).
     * Based on common complexity rules.
     * @param password The plaintext password.
     * returns Score from 0 to 100.
     */
    public static int calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;
        int length = password.length();

        // 1. Length Bonus (If the legth is a min of 20) add score 
        score += Math.min(20, length * 2);

        // 2. Character Set Diversity (upperCase, lowerCase, a digit and a symbol)
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");
        
        if (hasUpper) score += 15;
        if (hasLower) score += 15;
        if (hasDigit) score += 15;
        if (hasSymbol) score += 15;
        
        // Bonus for having multiple types
        int typeCount = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSymbol ? 1 : 0);
        if (typeCount >= 3) score += 5; //adding score of 5 if it has more than or 3 character sets

        return Math.min(100, score); // Limitting the score at 100
    }
}
