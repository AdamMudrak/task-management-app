package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Label;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class TaskRepositoryTest {
    private static final Logger logger = LogManager.getLogger(TaskRepositoryTest.class);
    @MockitoBean
    private final DbxClientV2 dbxClientV2 = null; //unused since not needed
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private LabelRepository labelRepository;
    private User testUser;
    private Project testProject;
    private Long anotherTestProjectId;
    private Long taskId;
    private Long labelId;
    private Long anotherLabelId;

    @BeforeAll
    public void setUpBeforeAll() {
        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);
        Role savedRole = roleRepository.save(role);

        User user = new User();
        user.setUsername(Constants.USERNAME);
        user.setPassword(Constants.PASSWORD);
        user.setEmail(Constants.EMAIL);
        user.setFirstName(Constants.FIRST_NAME);
        user.setLastName(Constants.LAST_NAME);
        user.setRole(savedRole);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        testUser = userRepository.save(user);

        Project project = new Project();
        project.setName(Constants.PROJECT_NAME);
        project.setDescription(Constants.PROJECT_DESCRIPTION);
        project.setStartDate(Constants.PROJECT_START_DATE);
        project.setEndDate(Constants.PROJECT_END_DATE);
        project.setStatus(Project.Status.INITIATED);
        project.setDeleted(false);
        project.setOwner(user);
        project.getManagers().add(user);
        project.getEmployees().add(user);
        testProject = projectRepository.save(project);

        Project anotherProject = new Project();
        anotherProject.setName(Constants.ANOTHER_PROJECT_NAME);
        anotherProject.setDescription(Constants.ANOTHER_PROJECT_DESCRIPTION);
        anotherProject.setStartDate(Constants.PROJECT_START_DATE);
        anotherProject.setEndDate(Constants.PROJECT_END_DATE);
        anotherProject.setStatus(Project.Status.INITIATED);
        anotherProject.setDeleted(true);
        anotherProject.setOwner(testUser);
        anotherProject.getManagers().add(testUser);
        anotherProject.getEmployees().add(testUser);
        anotherTestProjectId = projectRepository.save(anotherProject).getId();
    }

    /**Project, User and Role related tables have to be cleaned manually via sql
     *because of the Project entity soft delete logic
     */
    @SuppressWarnings("'Delete' statement without 'where' clears all data in the table")
    @AfterAll
    void tearDownAfterAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM project_employees");
            stmt.executeUpdate("DELETE FROM project_managers");
            stmt.executeUpdate("DELETE FROM projects");
            stmt.executeUpdate("DELETE FROM users");
            stmt.executeUpdate("DELETE FROM roles");
        }
        logger.info("DB clean up completed");
    }

    @BeforeEach
    public void setUp() {
        Task task = new Task();
        task.setName(Constants.TASK_NAME);
        task.setDescription(Constants.TASK_DESCRIPTION);
        task.setPriority(Task.Priority.LOW);
        task.setStatus(Task.Status.NOT_STARTED);
        task.setDueDate(Constants.TASK_DUE_DATE);
        task.setProject(testProject);
        task.setAssignee(testUser);
        task.setDeleted(false);
        taskId = taskRepository.save(task).getId();

        Label label = new Label();
        label.setName(Constants.LABEL_NAME);
        label.setUser(testUser);
        label.setColor(Label.Color.GREEN);
        label.getTasks().add(task);
        labelId = labelRepository.save(label).getId();

        Label labelWithNotTask = new Label();
        labelWithNotTask.setName(Constants.ANOTHER_LABEL_NAME);
        labelWithNotTask.setUser(testUser);
        labelWithNotTask.setColor(Label.Color.RED);
        anotherLabelId = labelRepository.save(labelWithNotTask).getId();
    }

    @Test
    void givenTaskAttachedToProject_whenFindAllByProjectIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByProjectIdNonDeleted(
                testProject.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenProjectWithNoTask_whenFindAllByProjectIdNonDeleted_thenReturnTask() {
        Assertions.assertTrue(taskRepository
                .findAllByProjectIdNonDeleted(anotherTestProjectId, Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTask_whenFindAllNonDeletedWithAssigneeAndProject_thenReturnTaskAndFetchedEntities() {
        List<Task> taskList = taskRepository.findAllNonDeletedWithAssigneeAndProject();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenTask_whenFindByIdNotDeleted_thenReturnTask() {
        Task task = taskRepository.findByIdNotDeleted(taskId).orElseThrow(
                () -> new EntityNotFoundException("Task with id " + taskId + " not found"));
        taskAssertions(task);
    }

    @Test
    void givenDeletedByProjectTask_whenFindByIdNotDeleted_thenReturnOptionalEmpty() {
        Assertions.assertTrue(taskRepository.findByIdNotDeleted(taskId).isPresent());
        taskRepository.deleteAllByProjectId(testProject.getId());
        Assertions.assertTrue(taskRepository.findByIdNotDeleted(taskId).isEmpty());
    }

    @Test
    void givenLabelAndTask_whenFindAllByLabelIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByLabelIdNonDeleted(
                labelId, Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenLabelWithNoTask_whenFindAllByLabelIdNonDeleted_thenReturnEmptyList() {
        Assertions.assertTrue(taskRepository.findAllByLabelIdNonDeleted(
                anotherLabelId, Pageable.unpaged()).isEmpty());
    }

    private void taskAssertions(Task task) {
        Assertions.assertNotNull(task);
        Assertions.assertEquals(taskId, task.getId());
        Assertions.assertEquals(Constants.TASK_NAME, task.getName());
        Assertions.assertEquals(Constants.TASK_DESCRIPTION, task.getDescription());
        Assertions.assertEquals(Task.Priority.LOW, task.getPriority());
        Assertions.assertEquals(Task.Status.NOT_STARTED, task.getStatus());
        Assertions.assertEquals(Constants.TASK_DUE_DATE, task.getDueDate());
        Assertions.assertEquals(testProject, task.getProject());
        Assertions.assertEquals(testUser, task.getAssignee());
        Assertions.assertFalse(task.isDeleted());
    }
}
