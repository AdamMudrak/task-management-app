package com.example.taskmanagementapp.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
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
    private static final String USERNAME_1 = "JohnDoe";
    private static final String USERNAME_2 = "RichardRoe";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";
    private static final String EMAIL_2 = "richard_roe@mail.com";
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
                        .username(USERNAME_1)
                        .password(PASSWORD_1_DB)
                        .email(EMAIL_1)
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .role(savedRole)
                        .isEnabled(true)
                        .isAccountNonLocked(true)
                        .build());
    }

    @Test
    void givenUser_whenFindByUsername_thenReturnUser() {
        User user = userRepository.findByUsername(USERNAME_1).orElseThrow(
                () -> new EntityNotFoundException("No user found with username \""
                        + USERNAME_1 + "\""));

        userAssertions(user);
    }

    @Test
    void givenNotExistingUser_whenFindByUsername_thenReturnEmptyUser() {
        assertTrue(userRepository.findByUsername(
                USERNAME_2).isEmpty());
    }

    @Test
    void givenUser_whenFindByEmail_thenReturnUser() {
        User user = userRepository.findByEmail(EMAIL_1).orElseThrow(
                () -> new EntityNotFoundException("No user found with username \""
                        + EMAIL_1 + "\""));

        userAssertions(user);
    }

    @Test
    void givenNotExistingUser_whenFindByEmail_thenReturnEmptyUser() {
        assertTrue(userRepository.findByEmail(
                EMAIL_2).isEmpty());
    }

    @Test
    void givenUser_whenExistsByUsername_thenReturnTrue() {
        assertTrue(userRepository.existsByUsername(USERNAME_1));
    }

    @Test
    void givenNotExistingUser_whenExistsByUsername_thenReturnFalse() {
        assertFalse(userRepository.existsByUsername(
                USERNAME_2));
    }

    @Test
    void givenUser_whenExistsByEmail_thenReturnTrue() {
        assertTrue(userRepository.existsByEmail(EMAIL_1));
    }

    @Test
    void givenNotExistingUser_whenExistsByEmail_thenReturnFalse() {
        assertFalse(userRepository.existsByEmail(EMAIL_2));
    }

    private void userAssertions(User user) {
        assertNotNull(user);
        assertEquals(USERNAME_1, user.getUsername());
        assertEquals(PASSWORD_1_DB, user.getPassword());
        assertEquals(EMAIL_1, user.getEmail());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(ROLE_USER, user.getRole().getName().name());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonLocked());
    }
}
