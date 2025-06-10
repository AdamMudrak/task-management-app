package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Role savedRole = roleRepository.save(ObjectFactory.getUserRole());
        userRepository.save(ObjectFactory.getUser1(savedRole));
    }

    @Test
    void givenUser_whenFindByUsername_thenReturnUser() {
        User user = userRepository.findByUsername(Constants.USERNAME).orElseThrow(
                () -> new EntityNotFoundException("No user found with username \""
                        + Constants.USERNAME + "\""));

        userAssertions(user);
    }

    @Test
    void givenNotExistingUser_whenFindByUsername_thenReturnEmptyUser() {
        Assertions.assertTrue(userRepository.findByUsername(
                Constants.ANOTHER_USERNAME).isEmpty());
    }

    @Test
    void givenUser_whenFindByEmail_thenReturnUser() {
        User user = userRepository.findByEmail(Constants.EMAIL).orElseThrow(
                () -> new EntityNotFoundException("No user found with username \""
                        + Constants.EMAIL + "\""));

        userAssertions(user);
    }

    @Test
    void givenNotExistingUser_whenFindByEmail_thenReturnEmptyUser() {
        Assertions.assertTrue(userRepository.findByEmail(
                Constants.ANOTHER_EMAIL).isEmpty());
    }

    @Test
    void givenUser_whenExistsByUsername_thenReturnTrue() {
        Assertions.assertTrue(userRepository.existsByUsername(Constants.USERNAME));
    }

    @Test
    void givenNotExistingUser_whenExistsByUsername_thenReturnFalse() {
        Assertions.assertFalse(userRepository.existsByUsername(
                Constants.ANOTHER_USERNAME));
    }

    @Test
    void givenUser_whenExistsByEmail_thenReturnTrue() {
        Assertions.assertTrue(userRepository.existsByEmail(Constants.EMAIL));
    }

    @Test
    void givenNotExistingUser_whenExistsByEmail_thenReturnFalse() {
        Assertions.assertFalse(userRepository.existsByEmail(Constants.ANOTHER_EMAIL));
    }

    private void userAssertions(User user) {
        Assertions.assertNotNull(user);
        Assertions.assertEquals(Constants.USERNAME, user.getUsername());
        Assertions.assertEquals(Constants.PASSWORD, user.getPassword());
        Assertions.assertEquals(Constants.EMAIL, user.getEmail());
        Assertions.assertEquals(Constants.FIRST_NAME, user.getFirstName());
        Assertions.assertEquals(Constants.LAST_NAME, user.getLastName());
        Assertions.assertEquals(Constants.ROLE_USER, user.getRole().getName().name());
        Assertions.assertTrue(user.isEnabled());
        Assertions.assertTrue(user.isAccountNonLocked());
    }
}
