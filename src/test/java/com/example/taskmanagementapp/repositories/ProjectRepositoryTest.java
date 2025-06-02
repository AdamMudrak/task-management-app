package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
public class ProjectRepositoryTest {
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    private User testUser;
    private User anotherTestUser;
    private Long existingProjectId;
    private Long deletedProjectId;

    @BeforeAll
    public void setUpBeforeAll() {
        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);
        Role savedRole = roleRepository.save(role);

        User testUser = new User();
        testUser.setUsername(Constants.USERNAME);
        testUser.setPassword(Constants.PASSWORD);
        testUser.setEmail(Constants.EMAIL);
        testUser.setFirstName(Constants.FIRST_NAME);
        testUser.setLastName(Constants.LAST_NAME);
        testUser.setRole(savedRole);
        testUser.setEnabled(true);
        testUser.setAccountNonLocked(true);
        this.testUser = userRepository.save(testUser);

        User anotherTestUser = new User();
        anotherTestUser.setUsername(Constants.ANOTHER_USERNAME);
        anotherTestUser.setPassword(Constants.PASSWORD);
        anotherTestUser.setEmail(Constants.ANOTHER_EMAIL);
        anotherTestUser.setFirstName(Constants.FIRST_NAME);
        anotherTestUser.setLastName(Constants.LAST_NAME);
        anotherTestUser.setRole(savedRole);
        anotherTestUser.setEnabled(true);
        anotherTestUser.setAccountNonLocked(true);
        this.anotherTestUser = userRepository.save(anotherTestUser);
    }

    @AfterAll
    public void tearDownAfterAll() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        Project project = new Project();
        project.setName(Constants.PROJECT_NAME);
        project.setDescription(Constants.PROJECT_DESCRIPTION);
        project.setStartDate(Constants.PROJECT_START_DATE);
        project.setEndDate(Constants.PROJECT_END_DATE);
        project.setStatus(Project.Status.INITIATED);
        project.setDeleted(false);
        project.setOwner(testUser);
        project.getManagers().add(testUser);
        project.getEmployees().add(testUser);
        existingProjectId = projectRepository.save(project).getId();

        Project deletedProject = new Project();
        deletedProject.setName(Constants.ANOTHER_PROJECT_NAME);
        deletedProject.setDescription(Constants.ANOTHER_PROJECT_DESCRIPTION);
        deletedProject.setStartDate(Constants.PROJECT_START_DATE);
        deletedProject.setEndDate(Constants.PROJECT_END_DATE);
        deletedProject.setStatus(Project.Status.INITIATED);
        deletedProject.setDeleted(true);
        deletedProject.setOwner(testUser);
        deletedProject.getManagers().add(testUser);
        deletedProject.getEmployees().add(testUser);
        deletedProjectId = projectRepository.save(deletedProject).getId();
    }

    @Test
    void givenUserWithNoProjects_whenFindAllByEmployeeId_thenReturnEmptyList() {
        Assertions.assertTrue(projectRepository.findAllByEmployeeId(
                anotherTestUser.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenUserWithNoProjects_whenFindAllByOwnerId_thenReturnEmptyList() {
        Assertions.assertTrue(projectRepository.findAllByOwnerId(
                anotherTestUser.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenNotDeletedProject_whenFindAllByOwnerId_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByOwnerId(testUser.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, projectList.size());
        projectAssertions(projectList.getFirst(), false,
                Constants.PROJECT_NAME, Constants.PROJECT_DESCRIPTION);
    }

    @Test
    void givenDeletedProject_whenFindAllByOwnerIdDeleted_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByOwnerIdDeleted(testUser.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, projectList.size());
        projectAssertions(projectList.getFirst(), true,
                Constants.ANOTHER_PROJECT_NAME, Constants.ANOTHER_PROJECT_DESCRIPTION);
    }

    @Test
    void givenNotDeletedProject_whenFindAllByEmployeeId_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByEmployeeId(testUser.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, projectList.size());
        projectAssertions(projectList.getFirst(), false,
                Constants.PROJECT_NAME, Constants.PROJECT_DESCRIPTION);
    }

    @Test
    void givenExistingProjectId_whenFindByIdNotDeleted_thenReturnSingleProject() {
        Project project = projectRepository.findByIdNotDeleted(existingProjectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + existingProjectId));
        projectAssertions(project, false, Constants.PROJECT_NAME, Constants.PROJECT_DESCRIPTION);
    }

    @Test
    void givenExistingProjectId_whenExistsByIdNotDeleted_thenReturnTrue() {
        Assertions.assertTrue(projectRepository.existsByIdNotDeleted(existingProjectId));
    }

    @Test
    void givenDeletedProjectId_whenExistsByIdNotDeleted_thenReturnFalse() {
        Assertions.assertFalse(projectRepository.existsByIdNotDeleted(deletedProjectId));
    }

    @Test
    void givenNotDeletedProject_whenIsUserSomebody_thenReturnTrue() {
        Assertions.assertTrue(projectRepository.isUserOwner(existingProjectId, testUser.getId()));
        Assertions.assertTrue(projectRepository.isUserManager(existingProjectId, testUser.getId()));
        Assertions.assertTrue(projectRepository
                .isUserEmployee(existingProjectId, testUser.getId()));
    }

    @Test
    void givenDeletedProject_whenIsUserSomebody_thenReturnFalse() {
        Assertions.assertFalse(projectRepository.isUserOwner(deletedProjectId, testUser.getId()));
        Assertions.assertFalse(projectRepository.isUserManager(deletedProjectId, testUser.getId()));
        Assertions.assertFalse(projectRepository
                .isUserEmployee(deletedProjectId, testUser.getId()));
    }

    private void projectAssertions(Project project, boolean isDeleted,
                                   String projectName, String projectDescription) {
        Assertions.assertNotNull(project);
        Assertions.assertEquals(projectName, project.getName());
        Assertions.assertEquals(projectDescription, project.getDescription());
        Assertions.assertEquals(Constants.PROJECT_START_DATE, project.getStartDate());
        Assertions.assertEquals(Constants.PROJECT_END_DATE, project.getEndDate());
        Assertions.assertEquals(Project.Status.INITIATED, project.getStatus());
        if (isDeleted) {
            Assertions.assertTrue(project.isDeleted());
        } else {
            Assertions.assertFalse(project.isDeleted());
        }
        Assertions.assertEquals(testUser.getId(), project.getOwner().getId());
        Assertions.assertEquals(1, project.getManagers().size());
        Assertions.assertEquals(1, project.getEmployees().size());
    }
}
