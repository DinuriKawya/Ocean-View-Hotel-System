package oceanview.service;

import oceanview.dao.UserDAO;
import oceanview.model.Role;
import oceanview.model.User;
import oceanview.model.UserStatus;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    public List<User> getAllUsers() throws UserException {
        try { return userDAO.findAll(); }
        catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    public User getById(int id) throws UserException {
        try {
            User u = userDAO.findById(id);
            if (u == null) throw new UserException("User #" + id + " not found.");
            return u;
        } catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    public int countByRole(Role role) throws UserException {
        try { return userDAO.countByRole(role); }
        catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    public int countByStatus(UserStatus status) throws UserException {
        try { return userDAO.countByStatus(status); }
        catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    public void createUser(String username, String fullName, Role role, String password,
                           String confirmPassword) throws UserException {

        if (username == null || username.isBlank())
            throw new UserException("Username is required.");
        if (fullName == null || fullName.isBlank())
            throw new UserException("Full name is required.");
        if (password == null || password.length() < 6)
            throw new UserException("Password must be at least 6 characters.");
        if (!password.equals(confirmPassword))
            throw new UserException("Passwords do not match.");

        try {
            if (userDAO.findByUsername(username.trim()) != null)
                throw new UserException("Username '" + username.trim() + "' is already taken.");

            User u = new User();
            u.setUsername(username.trim());
            u.setFullName(fullName.trim());
            u.setRole(role);
            u.setStatus(UserStatus.ACTIVE);
            u.setPasswordHash(sha256(password));
            userDAO.insert(u);

        } catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public void updateUser(int userId, String fullName, Role role) throws UserException {
        if (fullName == null || fullName.isBlank())
            throw new UserException("Full name is required.");
        try {
            User u = userDAO.findById(userId);
            if (u == null) throw new UserException("User not found.");
            u.setFullName(fullName.trim());
            u.setRole(role);
            userDAO.update(u);
        } catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    public void changePassword(int userId, String newPassword, String confirmPassword)
            throws UserException {
        if (newPassword == null || newPassword.length() < 6)
            throw new UserException("Password must be at least 6 characters.");
        if (!newPassword.equals(confirmPassword))
            throw new UserException("Passwords do not match.");
        try {
            if (userDAO.findById(userId) == null)
                throw new UserException("User not found.");
            userDAO.updatePasswordHash(userId, sha256(newPassword));
        } catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Status toggle
    // -----------------------------------------------------------------------

    public void setStatus(int userId, UserStatus status, int currentUserId)
            throws UserException {
        if (userId == currentUserId && status == UserStatus.INACTIVE)
            throw new UserException("You cannot deactivate your own account.");
        try {
            if (userDAO.findById(userId) == null)
                throw new UserException("User not found.");
            userDAO.updateStatus(userId, status);
        } catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    public void deleteUser(int userId, int currentUserId) throws UserException {
        if (userId == currentUserId)
            throw new UserException("You cannot delete your own account.");
        try {
            User u = userDAO.findById(userId);
            if (u == null) throw new UserException("User not found.");
            if (u.getRole() == Role.ADMIN && userDAO.countByRole(Role.ADMIN) <= 1)
                throw new UserException("Cannot delete the last admin account.");
            userDAO.delete(userId);
        } catch (SQLException e) { throw new UserException("Database error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String sha256(String input) throws UserException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UserException("Hashing algorithm unavailable.");
        }
    }

    // -----------------------------------------------------------------------
    // Checked exception
    // -----------------------------------------------------------------------

    public static class UserException extends Exception {
        public UserException(String message) { super(message); }
    }
}
