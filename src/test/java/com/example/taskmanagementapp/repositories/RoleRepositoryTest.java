package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Role;
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
class RoleRepositoryTest {
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.save(ObjectFactory.getUserRole());
        roleRepository.save(ObjectFactory.getAdminRole());
    }

    @Test
    void givenRole_whenFindByName_thenReturnRole() {
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER);
        Assertions.assertNotNull(userRole);
        Assertions.assertEquals(Role.RoleName.ROLE_USER, userRole.getName());

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN);
        Assertions.assertNotNull(adminRole);
        Assertions.assertEquals(Role.RoleName.ROLE_ADMIN, adminRole.getName());
    }
}
