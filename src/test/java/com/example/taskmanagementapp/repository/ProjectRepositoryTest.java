package com.example.taskmanagementapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProjectRepositoryTest {
    private static final String TEST_USERNAME = "JohnDoe";
    private static final String ANOTHER_TEST_USERNAME = "RichardRoe";
    private static final String TEST_PASSWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String TEST_EMAIL = "john_doe@mail.com";
    private static final String ANOTHER_TEST_EMAIL = "richard_roe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String ANOTHER_FIRST_NAME = "Richard";
    private static final String ANOTHER_LAST_NAME = "Roe";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    private static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    private User savedUser;
    private User anotherSavedUser;
    private Long existingProjectId;
    private Long deletedProjectId;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        savedUser = userRepository.save(
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

        anotherSavedUser = userRepository.save(
                User.builder()
                        .username(ANOTHER_TEST_USERNAME)
                        .password(TEST_PASSWORD_ENCODED)
                        .email(ANOTHER_TEST_EMAIL)
                        .firstName(ANOTHER_FIRST_NAME)
                        .lastName(ANOTHER_LAST_NAME)
                        .role(savedRole)
                        .isEnabled(true)
                        .isAccountNonLocked(true)
                        .build());
    }

    @AfterAll
    void tearDownAfterAll() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        existingProjectId = projectRepository.save(
                Project.builder()
                        .name(PROJECT_NAME)
                        .description(PROJECT_DESCRIPTION)
                        .startDate(PROJECT_START_DATE)
                        .endDate(PROJECT_END_DATE)
                        .status(Project.Status.IN_PROGRESS)
                        .isDeleted(false)
                        .owner(savedUser)
                        .managers(Set.of(savedUser))
                        .employees(Set.of(savedUser))
                        .build())
                .getId();
        
        deletedProjectId = projectRepository.save(
                Project.builder()
                        .name(ANOTHER_PROJECT_NAME)
                        .description(ANOTHER_PROJECT_DESCRIPTION)
                        .startDate(PROJECT_START_DATE)
                        .endDate(PROJECT_END_DATE)
                        .status(Project.Status.IN_PROGRESS)
                        .isDeleted(true)
                        .owner(savedUser)
                        .managers(Set.of(savedUser))
                        .employees(Set.of(savedUser))
                        .build())
                .getId();
    }

    @Test
    void givenUserWithNoProjects_whenFindAllByEmployeeId_thenReturnEmptyList() {
        assertTrue(projectRepository.findAllByEmployeeId(
                anotherSavedUser.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenUserWithNoProjects_whenFindAllByOwnerId_thenReturnEmptyList() {
        assertTrue(projectRepository.findAllByOwnerId(
                anotherSavedUser.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenNotDeletedProject_whenFindAllByOwnerId_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByOwnerId(savedUser.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, projectList.size());
        project(projectList.getFirst(), false,
                PROJECT_NAME, PROJECT_DESCRIPTION);
    }

    @Test
    void givenDeletedProject_whenFindAllByOwnerIdDeleted_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByOwnerIdDeleted(savedUser.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, projectList.size());
        project(projectList.getFirst(), true,
                ANOTHER_PROJECT_NAME, ANOTHER_PROJECT_DESCRIPTION);
    }

    @Test
    void givenNotDeletedProject_whenFindAllByEmployeeId_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByEmployeeId(savedUser.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, projectList.size());
        project(projectList.getFirst(), false,
                PROJECT_NAME, PROJECT_DESCRIPTION);
    }

    @Test
    void givenExistingProjectId_whenFindByIdNotDeleted_thenReturnSingleProject() {
        Project project = projectRepository.findByIdNotDeleted(existingProjectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + existingProjectId));
        project(project, false, PROJECT_NAME, PROJECT_DESCRIPTION);
    }

    @Test
    void givenExistingProjectId_whenExistsByIdNotDeleted_thenReturnTrue() {
        assertTrue(projectRepository.existsByIdNotDeleted(existingProjectId));
    }

    @Test
    void givenDeletedProjectId_whenExistsByIdNotDeleted_thenReturnFalse() {
        assertFalse(projectRepository.existsByIdNotDeleted(deletedProjectId));
    }

    @Test
    void givenNotDeletedProject_whenIsUserSomebody_thenReturnTrue() {
        assertTrue(projectRepository.isUserOwner(existingProjectId, savedUser.getId()));
        assertTrue(projectRepository.isUserManager(existingProjectId, savedUser.getId()));
        assertTrue(projectRepository
                .isUserEmployee(existingProjectId, savedUser.getId()));
    }

    @Test
    void givenDeletedProject_whenIsUserSomebody_thenReturnFalse() {
        assertFalse(projectRepository.isUserOwner(deletedProjectId, savedUser.getId()));
        assertFalse(projectRepository.isUserManager(deletedProjectId, savedUser.getId()));
        assertFalse(projectRepository
                .isUserEmployee(deletedProjectId, savedUser.getId()));
    }

    private void project(Project project, boolean isDeleted,
                                   String projectName, String projectDescription) {
        assertNotNull(project);
        assertEquals(projectName, project.getName());
        assertEquals(projectDescription, project.getDescription());
        assertEquals(PROJECT_START_DATE, project.getStartDate());
        assertEquals(PROJECT_END_DATE, project.getEndDate());
        assertEquals(Project.Status.IN_PROGRESS, project.getStatus());
        if (isDeleted) {
            assertTrue(project.isDeleted());
        } else {
            assertFalse(project.isDeleted());
        }
        assertEquals(savedUser.getId(), project.getOwner().getId());
        assertEquals(1, project.getManagers().size());
        assertEquals(1, project.getEmployees().size());
    }
}
