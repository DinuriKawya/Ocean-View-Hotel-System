package oceanview.test;

import oceanview.dao.UserDAO;
import oceanview.model.Role;
import oceanview.model.User;
import oceanview.model.UserStatus;
import oceanview.service.UserService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserDAO mockDAO;
    private UserService service;
    private User sampleUser;

    @Before
    public void setUp() {

        mockDAO = Mockito.mock(UserDAO.class);
        service = new UserService(mockDAO);

        sampleUser = new User();
        sampleUser.setUserId(1);
        sampleUser.setUsername("admin");
        sampleUser.setFullName("Admin User");
        sampleUser.setRole(Role.ADMIN);
        sampleUser.setStatus(UserStatus.ACTIVE);
    }

    // ----------------------------------------------------
    // getAllUsers
    // ----------------------------------------------------

    @Test
    public void testGetAllUsers_success_returnsList() throws Exception {

        when(mockDAO.findAll()).thenReturn(Arrays.asList(sampleUser));

        List<User> result = service.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllUsers_empty_returnsEmptyList() throws Exception {

        when(mockDAO.findAll()).thenReturn(Collections.emptyList());

        List<User> result = service.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test(expected = UserService.UserException.class)
    public void testGetAllUsers_sqlException() throws Exception {

        when(mockDAO.findAll()).thenThrow(new SQLException());

        service.getAllUsers();
    }

    // ----------------------------------------------------
    // getById
    // ----------------------------------------------------

    @Test
    public void testGetById_success() throws Exception {

        when(mockDAO.findById(1)).thenReturn(sampleUser);

        User result = service.getById(1);

        assertEquals("admin", result.getUsername());
    }

    @Test(expected = UserService.UserException.class)
    public void testGetById_userNotFound() throws Exception {

        when(mockDAO.findById(99)).thenReturn(null);

        service.getById(99);
    }

    // ----------------------------------------------------
    // createUser
    // ----------------------------------------------------

    @Test
    public void testCreateUser_success() throws Exception {

        when(mockDAO.findByUsername("admin")).thenReturn(null);

        service.createUser("admin","Admin User",
                Role.ADMIN,"password123","password123");

        verify(mockDAO).insert(any(User.class));
    }

    @Test(expected = UserService.UserException.class)
    public void testCreateUser_usernameExists() throws Exception {

        when(mockDAO.findByUsername("admin")).thenReturn(sampleUser);

        service.createUser("admin","Admin User",
                Role.ADMIN,"password123","password123");
    }

    // ----------------------------------------------------
    // updateUser
    // ----------------------------------------------------

    @Test
    public void testUpdateUser_success() throws Exception {

        when(mockDAO.findById(1)).thenReturn(sampleUser);

        service.updateUser(1,"New Name",Role.STAFF);

        verify(mockDAO).update(any(User.class));
    }

    @Test(expected = UserService.UserException.class)
    public void testUpdateUser_notFound() throws Exception {

        when(mockDAO.findById(1)).thenReturn(null);

        service.updateUser(1,"Name",Role.ADMIN);
    }

    // ----------------------------------------------------
    // deleteUser
    // ----------------------------------------------------

    @Test
    public void testDeleteUser_success() throws Exception {

        when(mockDAO.findById(1)).thenReturn(sampleUser);
        when(mockDAO.countByRole(Role.ADMIN)).thenReturn(2);

        service.deleteUser(1,2);

        verify(mockDAO).delete(1);
    }

    @Test(expected = UserService.UserException.class)
    public void testDeleteUser_selfDelete() throws Exception {

        service.deleteUser(1,1);
    }

    @Test(expected = UserService.UserException.class)
    public void testDeleteUser_lastAdmin() throws Exception {

        when(mockDAO.findById(1)).thenReturn(sampleUser);
        when(mockDAO.countByRole(Role.ADMIN)).thenReturn(1);

        service.deleteUser(1,2);
    }

    // ----------------------------------------------------
    // changePassword
    // ----------------------------------------------------

    @Test
    public void testChangePassword_success() throws Exception {

        when(mockDAO.findById(1)).thenReturn(sampleUser);

        service.changePassword(1,"newpass123","newpass123");

        verify(mockDAO).updatePasswordHash(eq(1), anyString());
    }

    // ----------------------------------------------------
    // setStatus
    // ----------------------------------------------------

    @Test
    public void testSetStatus_success() throws Exception {

        when(mockDAO.findById(1)).thenReturn(sampleUser);

        service.setStatus(1,UserStatus.INACTIVE,2);

        verify(mockDAO).updateStatus(1,UserStatus.INACTIVE);
    }

    @Test(expected = UserService.UserException.class)
    public void testSetStatus_selfDeactivate() throws Exception {

        service.setStatus(1,UserStatus.INACTIVE,1);
    }

    // ----------------------------------------------------
    // count methods
    // ----------------------------------------------------

    @Test
    public void testCountByRole() throws Exception {

        when(mockDAO.countByRole(Role.ADMIN)).thenReturn(3);

        int result = service.countByRole(Role.ADMIN);

        assertEquals(3,result);
    }

    @Test
    public void testCountByStatus() throws Exception {

        when(mockDAO.countByStatus(UserStatus.ACTIVE)).thenReturn(5);

        int result = service.countByStatus(UserStatus.ACTIVE);

        assertEquals(5,result);
    }
    
 // ----------------------------------------------------
 // Validation & edge cases
 // ----------------------------------------------------

 @Test(expected = UserService.UserException.class)
 public void testCreateUser_passwordMismatch() throws Exception {
     when(mockDAO.findByUsername("user")).thenReturn(null);
     service.createUser("user", "Full Name", Role.STAFF, "pass123", "pass124");
 }

 @Test(expected = UserService.UserException.class)
 public void testUpdateUser_emptyFullName() throws Exception {
     service.updateUser(1, "", Role.ADMIN);
 }

 @Test(expected = UserService.UserException.class)
 public void testSetStatus_deactivateSelf() throws Exception {
     when(mockDAO.findById(1)).thenReturn(sampleUser);
     service.setStatus(1, UserStatus.INACTIVE, 1);
 }

 @Test(expected = UserService.UserException.class)
 public void testDeleteUser_deleteSelf() throws Exception {
     service.deleteUser(1, 1);
 }

 // ----------------------------------------------------
 // DAO Exceptions
 // ----------------------------------------------------

 @Test(expected = UserService.UserException.class)
 public void testGetById_daoThrows() throws Exception {
     when(mockDAO.findById(1)).thenThrow(new SQLException("DB Error"));
     service.getById(1);
 }

 @Test(expected = UserService.UserException.class)
 public void testCreateUser_daoThrows() throws Exception {
     when(mockDAO.findByUsername("user")).thenReturn(null);
     doThrow(new SQLException("DB Error")).when(mockDAO).insert(any(User.class));
     service.createUser("user", "Full Name", Role.STAFF, "password123", "password123");
 }

 @Test(expected = UserService.UserException.class)
 public void testUpdateUser_daoThrows() throws Exception {
     when(mockDAO.findById(1)).thenReturn(sampleUser);
     doThrow(new SQLException("DB Error")).when(mockDAO).update(any(User.class));
     service.updateUser(1, "New Name", Role.ADMIN);
 }

 @Test(expected = UserService.UserException.class)
 public void testDeleteUser_daoThrows() throws Exception {
     when(mockDAO.findById(1)).thenReturn(sampleUser);
     doThrow(new SQLException("DB Error")).when(mockDAO).delete(1);
     service.deleteUser(1, 2);
 }

 @Test(expected = UserService.UserException.class)
 public void testSetStatus_daoThrows() throws Exception {
     when(mockDAO.findById(1)).thenReturn(sampleUser);
     doThrow(new SQLException("DB Error")).when(mockDAO).updateStatus(1, UserStatus.ACTIVE);
     service.setStatus(1, UserStatus.ACTIVE, 2);
 }

}