package com.example.taskmanagementapp.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    private static final String TEST_USERNAME = "JohnDoe";
    private static final String ANOTHER_TEST_USERNAME = "RichardRoe";
    private static final String TEST_PASSWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String TEST_EMAIL = "john_doe@mail.com";
    private static final String ANOTHER_TEST_EMAIL = "richard_roe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String ROLE_USER = "ROLE_USER";
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        userRepository.save(
                User.builder()
                        .username(TEST_USERNAME)
                        .password(TEST_PASSWORD_ENCODED)
                        .email(TEST_EMAIL)
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .role(savedRole)
                        .isEnabled(true)
                        .isAccountNonLocked(true)
                        .build());
    }

    @Test
    void givenUser_whenFindByUsername_thenReturnUser() {
        User user = userRepository.findByUsername(TEST_USERNAME).orElseThrow(
                () -> new EntityNotFoundException("No user found with username \""
                        + TEST_USERNAME + "\""));

        userAssertions(user);
    }

    @Test
    void givenNotExistingUser_whenFindByUsername_thenReturnEmptyUser() {
        assertTrue(userRepository.findByUsername(
                ANOTHER_TEST_USERNAME).isEmpty());
    }

    @Test
    void givenUser_whenFindByEmail_thenReturnUser() {
        User user = userRepository.findByEmail(TEST_EMAIL).orElseThrow(
                () -> new EntityNotFoundException("No user found with username \""
                        + TEST_EMAIL + "\""));

        userAssertions(user);
    }

    @Test
    void givenNotExistingUser_whenFindByEmail_thenReturnEmptyUser() {
        assertTrue(userRepository.findByEmail(
                ANOTHER_TEST_EMAIL).isEmpty());
    }

    @Test
    void givenUser_whenExistsByUsername_thenReturnTrue() {
        assertTrue(userRepository.existsByUsername(TEST_USERNAME));
    }

    @Test
    void givenNotExistingUser_whenExistsByUsername_thenReturnFalse() {
        assertFalse(userRepository.existsByUsername(
                ANOTHER_TEST_USERNAME));
    }

    @Test
    void givenUser_whenExistsByEmail_thenReturnTrue() {
        assertTrue(userRepository.existsByEmail(TEST_EMAIL));
    }

    @Test
    void givenNotExistingUser_whenExistsByEmail_thenReturnFalse() {
        assertFalse(userRepository.existsByEmail(ANOTHER_TEST_EMAIL));
    }

    private void userAssertions(User user) {
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_PASSWORD_ENCODED, user.getPassword());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(ROLE_USER, user.getRole().getName().name());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonLocked());
    }
}
