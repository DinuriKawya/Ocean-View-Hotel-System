package oceanview.test;

import oceanview.dao.UserDAO;
import oceanview.model.Role;
import oceanview.model.User;
import oceanview.model.UserStatus;
import oceanview.service.AuthService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.MessageDigest;
import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserDAO mockDao;
    private AuthService service;
    private User activeUser;

    // builds sha256 hash 
    private String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    @Before
    public void setUp() throws Exception {
        mockDao = Mockito.mock(UserDAO.class);
        service = new AuthService(mockDao);

        activeUser = new User();
        activeUser.setUserId(1);
        activeUser.setUsername("admin");
        activeUser.setPasswordHash(sha256("password123"));
        activeUser.setFullName("Admin User");
        activeUser.setRole(Role.ADMIN);
        activeUser.setStatus(UserStatus.ACTIVE);
    }

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------

    @Test
    public void testGetInstance_notNull() {
        assertNotNull(AuthService.getInstance());
    }

    @Test
    public void testGetInstance_returnsSameInstance() {
        AuthService a = AuthService.getInstance();
        AuthService b = AuthService.getInstance();
        assertSame(a, b);
    }

    // -----------------------------------------------------------------------
    // login — null / blank validation
    // -----------------------------------------------------------------------

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_nullUsername_throwsAuthException() throws Exception {
        service.login(null, "password123");
    }

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_nullPassword_throwsAuthException() throws Exception {
        service.login("admin", null);
    }

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_blankUsername_throwsAuthException() throws Exception {
        service.login("   ", "password123");
    }

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_blankPassword_throwsAuthException() throws Exception {
        service.login("admin", "   ");
    }

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_emptyUsername_throwsAuthException() throws Exception {
        service.login("", "password123");
    }

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_emptyPassword_throwsAuthException() throws Exception {
        service.login("admin", "");
    }

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_bothNull_throwsAuthException() throws Exception {
        service.login(null, null);
    }

    @Test
    public void testLogin_blankInputs_correctMessage() {
        try {
            service.login("", "");
            fail("Expected AuthException");
        } catch (AuthService.AuthException e) {
            assertEquals("Username and password are required.", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // login — user not found
    // -----------------------------------------------------------------------

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_userNotFound_throwsAuthException() throws Exception {
        when(mockDao.findByUsername("unknown")).thenReturn(null);
        service.login("unknown", "password123");
    }

    @Test
    public void testLogin_userNotFound_correctMessage() throws Exception {
        when(mockDao.findByUsername("unknown")).thenReturn(null);
        try {
            service.login("unknown", "password123");
            fail("Expected AuthException");
        } catch (AuthService.AuthException e) {
            assertEquals("Invalid username or password.", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // login — wrong password
    // -----------------------------------------------------------------------

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_wrongPassword_throwsAuthException() throws Exception {
        when(mockDao.findByUsername("admin")).thenReturn(activeUser);
        service.login("admin", "wrongpassword");
    }

    @Test
    public void testLogin_wrongPassword_correctMessage() throws Exception {
        when(mockDao.findByUsername("admin")).thenReturn(activeUser);
        try {
            service.login("admin", "wrongpassword");
            fail("Expected AuthException");
        } catch (AuthService.AuthException e) {
            assertEquals("Invalid username or password.", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // login — inactive account
    // -----------------------------------------------------------------------

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_inactiveUser_throwsAuthException() throws Exception {
        activeUser.setStatus(UserStatus.INACTIVE);
        when(mockDao.findByUsername("admin")).thenReturn(activeUser);
        service.login("admin", "password123");
    }

    @Test
    public void testLogin_inactiveUser_correctMessage() throws Exception {
        activeUser.setStatus(UserStatus.INACTIVE);
        when(mockDao.findByUsername("admin")).thenReturn(activeUser);
        try {
            service.login("admin", "password123");
            fail("Expected AuthException");
        } catch (AuthService.AuthException e) {
            assertEquals("Account is inactive. Please contact an administrator.", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // login — success
    // -----------------------------------------------------------------------

    @Test
    public void testLogin_success_returnsUser() throws Exception {
        when(mockDao.findByUsername("admin")).thenReturn(activeUser);
        User result = service.login("admin", "password123");
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
    }

    @Test
    public void testLogin_success_trimsUsername() throws Exception {
        when(mockDao.findByUsername("admin")).thenReturn(activeUser);
        User result = service.login("  admin  ", "password123");
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    // -----------------------------------------------------------------------
    // login — SQL exception
    // -----------------------------------------------------------------------

    @Test(expected = AuthService.AuthException.class)
    public void testLogin_sqlException_throwsAuthException() throws Exception {
        when(mockDao.findByUsername("admin")).thenThrow(new SQLException("DB error"));
        service.login("admin", "password123");
    }

    @Test
    public void testLogin_sqlException_correctMessage() throws Exception {
        when(mockDao.findByUsername("admin")).thenThrow(new SQLException("DB error"));
        try {
            service.login("admin", "password123");
            fail("Expected AuthException");
        } catch (AuthService.AuthException e) {
            assertTrue(e.getMessage().contains("Database error during login"));
        }
    }

    // -----------------------------------------------------------------------
    // AuthException
    // -----------------------------------------------------------------------

    @Test
    public void testAuthException_message() {
        AuthService.AuthException ex = new AuthService.AuthException("Test error");
        assertEquals("Test error", ex.getMessage());
    }
}
