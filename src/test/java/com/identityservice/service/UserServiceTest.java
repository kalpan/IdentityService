package com.identityservice.service;

import com.identityservice.dto.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @TestConfiguration
    static class UserServiceImplTestContextConfiguration {

        @Bean
        public UserService userService() {
            return new UserServiceImpl();
        }
    }

    @Autowired
    UserService userService;

    @Test
    public void findById() throws Exception {
    	User user = new User("moe", "doe", "mdoe", "password");
    	userService.saveUser(user);
    	Long id = userService.findByUserName(user.getUserName()).getId();
        assertEquals("mdoe", userService.findById(id).getUserName());
    }

    @Test
    public void findByUserName() throws Exception {
        User user = new User("ffirstName", "flastName", "fuserName", "fpassword");
        userService.saveUser(user);
        assertEquals("ffirstName", userService.findByUserName("fuserName").getFirstName());
    }

    @Test
    public void findByUserNameAsync() throws Exception {
        User user = new User("afirstName", "alastName", "auserName", "apassword");
        userService.saveUser(user);
        assertEquals("afirstName", userService.findByUserNameAsync("auserName").get().getFirstName());
    }

    @Test
    public void saveUser() throws Exception {
        User user = new User("firstName", "lastName", "userName", "password");
        userService.saveUser(user);
        assertEquals("firstName", userService.findByUserName("userName").getFirstName());
    }

    @Test
    public void updateUser() throws Exception {
        User user = new User("ufirstName", "ulastName", "uuserName", "upassword");
        userService.saveUser(user);
        assertEquals("upassword", userService.findByUserName("uuserName").getPassword());
        userService.saveUser(userService.findByUserName("uuserName").setPassword("updatedPassword"));
        assertEquals("updatedPassword", userService.findByUserName("uuserName").getPassword());
    }

    @Test
    public void deleteUserById() throws Exception {
        User user = new User("dfirstName", "dlastName", "duserName", "dpassword");
        userService.saveUser(user);
        assertEquals("dfirstName", userService.findByUserName("duserName").getFirstName());
        userService.deleteUserById(user.getId());
        assertTrue(userService.findByUserName("duserName") == null);
    }

    @Test
    public void findAllUsers() throws Exception {
        List<User> users = userService.findAllUsers();
        assertTrue(users.size() > 0);
    }

    @Test
    public void deleteAllUsers() throws Exception {
        userService.deleteAllUsers();
        List<User> users = userService.findAllUsers();
        assertTrue(users.size() == 0);
    }

    @Test
    public void isUserExist() throws Exception {
        User user = new User("john", "doe", "jdoe", "password");
        userService.saveUser(user);
        assertTrue(userService.isUserExist(user));
    }

}