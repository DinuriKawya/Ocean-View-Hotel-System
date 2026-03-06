package oceanview.service;

import oceanview.dao.UserDAO;
import oceanview.model.User;
import oceanview.model.UserStatus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class AuthService {

    // singleton instance
    private static AuthService instance;

    private final UserDAO dao;


    private AuthService() { this.dao = new UserDAO(); }


    public AuthService(UserDAO dao) { this.dao = dao; }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public User login(String username, String password) throws AuthException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Username and password are required.");
        }

        try {
            User user = dao.findByUsername(username.trim());

            if (user == null) {
                throw new AuthException("Invalid username or password.");
            }

            String inputHash = sha256(password);
            if (!user.getPasswordHash().equals(inputHash)) {
                throw new AuthException("Invalid username or password.");
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new AuthException("Account is inactive. Please contact an administrator.");
            }

            return user;

        } catch (SQLException e) {
            throw new AuthException("Database error during login: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String sha256(String input) throws AuthException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AuthException("Hashing algorithm unavailable.");
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }
}
