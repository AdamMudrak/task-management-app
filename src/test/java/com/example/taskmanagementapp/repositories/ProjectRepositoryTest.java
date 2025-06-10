package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
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
class ProjectRepositoryTest {
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    private User user1;
    private User user2;
    private Long existingProjectId;
    private Long deletedProjectId;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(ObjectFactory.getUserRole());
        user1 = userRepository.save(ObjectFactory.getUser1(savedRole));
        user2 = userRepository.save(ObjectFactory.getUser2(savedRole));
    }

    @AfterAll
    void tearDownAfterAll() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        Project project1 = projectRepository.save(ObjectFactory.getProjectWithOneEmployee(user1));
        existingProjectId = project1.getId();

        Project project2 = projectRepository.save(ObjectFactory.getDeletedProject(user1));
        deletedProjectId = project2.getId();
    }

    @Test
    void givenUserWithNoProjects_whenFindAllByEmployeeId_thenReturnEmptyList() {
        Assertions.assertTrue(projectRepository.findAllByEmployeeId(
                user2.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenUserWithNoProjects_whenFindAllByOwnerId_thenReturnEmptyList() {
        Assertions.assertTrue(projectRepository.findAllByOwnerId(
                user2.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenNotDeletedProject_whenFindAllByOwnerId_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByOwnerId(user1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, projectList.size());
        projectAssertions(projectList.getFirst(), false,
                Constants.PROJECT_NAME, Constants.PROJECT_DESCRIPTION);
    }

    @Test
    void givenDeletedProject_whenFindAllByOwnerIdDeleted_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByOwnerIdDeleted(user1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, projectList.size());
        projectAssertions(projectList.getFirst(), true,
                Constants.ANOTHER_PROJECT_NAME, Constants.ANOTHER_PROJECT_DESCRIPTION);
    }

    @Test
    void givenNotDeletedProject_whenFindAllByEmployeeId_thenReturnSingleProject() {
        List<Project> projectList = projectRepository
                .findAllByEmployeeId(user1.getId(), Pageable.unpaged()).getContent();
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
        Assertions.assertTrue(projectRepository.isUserOwner(existingProjectId, user1.getId()));
        Assertions.assertTrue(projectRepository.isUserManager(existingProjectId, user1.getId()));
        Assertions.assertTrue(projectRepository
                .isUserEmployee(existingProjectId, user1.getId()));
    }

    @Test
    void givenDeletedProject_whenIsUserSomebody_thenReturnFalse() {
        Assertions.assertFalse(projectRepository.isUserOwner(deletedProjectId, user1.getId()));
        Assertions.assertFalse(projectRepository.isUserManager(deletedProjectId, user1.getId()));
        Assertions.assertFalse(projectRepository
                .isUserEmployee(deletedProjectId, user1.getId()));
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
        Assertions.assertEquals(user1.getId(), project.getOwner().getId());
        Assertions.assertEquals(1, project.getManagers().size());
        Assertions.assertEquals(1, project.getEmployees().size());
    }
}
