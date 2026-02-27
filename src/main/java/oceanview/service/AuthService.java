package oceanview.service;

import oceanview.database.DBConnection;
import oceanview.model.Role;
import oceanview.model.User;
import oceanview.model.UserStatus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    /**
     * Attempts to log in a user.
     *
     * @param username plain-text username
     * @param password plain-text password (will be SHA-256 hashed before comparison)
     * @return the matching User if credentials are valid and account is ACTIVE
     * @throws AuthException with a descriptive message on any failure
     */
    public User login(String username, String password) throws AuthException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Username and password are required.");
        }

        String sql = "SELECT user_id, username, password_hash, full_name, role, status " +
                     "FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new AuthException("Invalid username or password.");
            }

            String storedHash  = rs.getString("password_hash");
            String inputHash   = sha256(password);

            if (!storedHash.equals(inputHash)) {
                throw new AuthException("Invalid username or password.");
            }

            UserStatus status = UserStatus.valueOf(rs.getString("status").toUpperCase());
            if (status != UserStatus.ACTIVE) {
                throw new AuthException("Account is inactive. Please contact an administrator.");
            }

            User user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setUsername(rs.getString("username"));
            user.setPasswordHash(storedHash);
            user.setFullName(rs.getString("full_name"));
            user.setRole(Role.valueOf(rs.getString("role").toUpperCase()));
            user.setStatus(status);

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

    // -----------------------------------------------------------------------
    // Checked exception used by this service
    // -----------------------------------------------------------------------

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }
}
