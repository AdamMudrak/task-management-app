package com.example.taskmanagementapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.Label;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
class TaskRepositoryTest {
    private static final String TEST_USERNAME = "JohnDoe";
    private static final String TEST_PASSWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String TEST_EMAIL = "john_doe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    private static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final String TASK_NAME = "taskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);
    private static final String LABEL_NAME = "labelName";
    private static final String ANOTHER_LABEL_NAME = "anotherLabelName";
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
    private User savedUser;
    private Project livingProject;
    private Project deletedProject;
    private Task task;
    private Label livingLabel;
    private Label labelWithNoTask;

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

        livingProject = projectRepository.save(projectRepository.save(
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
                        .build()));

        deletedProject = projectRepository.save(projectRepository.save(
                Project.builder()
                        .name(ANOTHER_PROJECT_NAME)
                        .description(ANOTHER_PROJECT_DESCRIPTION)
                        .startDate(PROJECT_START_DATE)
                        .endDate(PROJECT_END_DATE)
                        .status(Project.Status.COMPLETED)
                        .isDeleted(true)
                        .owner(savedUser)
                        .managers(Set.of(savedUser))
                        .employees(Set.of(savedUser))
                        .build()));
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
    void setUp() {
        task = taskRepository.save(
                Task.builder()
                        .name(TASK_NAME)
                        .description(TASK_DESCRIPTION)
                        .priority(Task.Priority.LOW)
                        .status(Task.Status.NOT_STARTED)
                        .dueDate(TASK_DUE_DATE)
                        .project(livingProject)
                        .assignee(savedUser)
                        .isDeleted(false)
                        .build());

        livingLabel = labelRepository.save(
                        Label.builder()
                                .name(LABEL_NAME)
                                .color(Label.Color.RED)
                                .user(savedUser)
                                .tasks(Set.of(task))
                                .build());

        labelWithNoTask = labelRepository.save(
                        Label.builder()
                                .name(ANOTHER_LABEL_NAME)
                                .color(Label.Color.YELLOW)
                                .user(savedUser)
                                .build());
    }

    @Test
    void givenTaskAttachedToProject_whenFindAllByProjectIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByProjectIdNonDeleted(
                livingProject.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenProjectWithNoTask_whenFindAllByProjectIdNonDeleted_thenReturnEmpty() {
        assertTrue(taskRepository
                .findAllByProjectIdNonDeleted(
                        deletedProject.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTask_whenFindAllNonDeletedWithAssigneeAndProject_thenReturnTaskAndFetchedEntities() {
        List<Task> taskList = taskRepository.findAllNonDeletedWithAssigneeAndProject();
        assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenTask_whenFindByIdNotDeleted_thenReturnTask() {
        Task task = taskRepository.findByIdNotDeleted(this.task.getId()).orElseThrow(
                () -> new EntityNotFoundException("Task with id " + this.task.getId() + " not found"));
        taskAssertions(task);
    }

    @Test
    void givenDeletedByProjectTask_whenFindByIdNotDeleted_thenReturnOptionalEmpty() {
        assertTrue(taskRepository.findByIdNotDeleted(task.getId()).isPresent());
        taskRepository.deleteAllByProjectId(livingProject.getId());
        assertTrue(taskRepository.findByIdNotDeleted(task.getId()).isEmpty());
    }

    @Test
    void givenLabelAndTask_whenFindAllByLabelIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByLabelIdNonDeleted(
                livingLabel.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenLabelWithNoTask_whenFindAllByLabelIdNonDeleted_thenReturnEmptyList() {
        assertTrue(taskRepository.findAllByLabelIdNonDeleted(
                labelWithNoTask.getId(), Pageable.unpaged()).isEmpty());
    }

    private void taskAssertions(Task task) {
        assertNotNull(task);
        assertEquals(this.task.getId(), task.getId());
        assertEquals(TASK_NAME, task.getName());
        assertEquals(TASK_DESCRIPTION, task.getDescription());
        assertEquals(Task.Priority.LOW, task.getPriority());
        assertEquals(Task.Status.NOT_STARTED, task.getStatus());
        assertEquals(TASK_DUE_DATE, task.getDueDate());
        assertEquals(livingProject, task.getProject());
        assertEquals(savedUser, task.getAssignee());
        assertFalse(task.isDeleted());
    }
}
