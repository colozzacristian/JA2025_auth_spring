package it.eforhum.auth_module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.eforhum.auth_module.entity.User;
import it.eforhum.auth_module.repository.UserDAOImp;



public class UserDaoTest {

    @Autowired
    private  UserDAOImp userDao;

    @Value("${url.backoffice}")
    private String backofficeUrl;



    @Test
    public void testGetByEmail_existingUser_shouldReturnUser() {
        User u = userDao.getByEmail("a@a.a");
        assertNotNull("User should not be null", u);
        assertEquals("Email should match", "a@a.a", u.getEmail());
    }

    @Test
    public void testGetByEmail_nonExistingUser_shouldReturnNull() {
        User u = userDao.getByEmail("nonexistent@a.a");
        assertEquals("User should be null for non-existing email", null, u);
    }

    @Test
    public void testLogin_correctCredentials_shouldReturnUser() {
        User u = userDao.login("a@a.a", "password123");
        assertNotNull("Login should succeed with correct credentials", u);
        assertEquals("Email should match", "a@a.a", u.getEmail());
    }

    @Test
    public void testLogin_incorrectCredentials_shouldReturnNull() {
        User u = userDao.login("a@a.a", "wrongpassword");
        assertEquals("Login should fail with incorrect credentials", null, u);
    }

    @Test
    public void testCreate_newUserSameEmail_shouldReturnNull() {
        User u = userDao.create("a@a.a", "newpassword", "New", "User");
        assertEquals("Creating a user with an existing email should return null", null, u);
    }

    @Test
    public void testActivateUser() {
        User u = userDao.getByEmail("a@a.a");
        assertNotNull("User should exist before activation", u);
        boolean activated = userDao.activateUser(u);
        assertEquals("User activation should return true", true, activated);
        u = userDao.getByEmail("a@a.a");
        assertEquals("User should be activated", true, u.isActive());
    }

    @Test
    public void testActivateUser_nonExistingUser_shouldReturnFalse() {
        User u = new User(99999l,"nonexistent@a.a", "First", "Last", "hashedpassword", false);
        boolean activated = userDao.activateUser(u);
        assertEquals("Activating a non-existing user should return false", false, activated);
    }
   

}