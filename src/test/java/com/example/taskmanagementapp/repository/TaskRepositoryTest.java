package com.example.taskmanagementapp.repository;

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
class TaskRepositoryTest {
    private static final String USERNAME_1 = "JohnDoe";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    private static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final String TASK_NAME_1 = "taskName";
    private static final String TASK_DESCRIPTION_1 = "taskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);
    private static final String LABEL_NAME_1 = "labelName";
    private static final String LABEL_NAME_2 = "anotherLabelName";
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
    private User user1;
    private Project livingProject;
    private Project deletedProject;
    private Task task1;
    private Label livingLabel;
    private Label labelWithNoTask;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        user1 = userRepository.save(
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

        livingProject = projectRepository.save(projectRepository.save(
                Project.builder()
                        .name(PROJECT_NAME)
                        .description(PROJECT_DESCRIPTION)
                        .startDate(PROJECT_START_DATE)
                        .endDate(PROJECT_END_DATE)
                        .status(Project.Status.IN_PROGRESS)
                        .isDeleted(false)
                        .owner(user1)
                        .managers(Set.of(user1))
                        .employees(Set.of(user1))
                        .build()));

        deletedProject = projectRepository.save(projectRepository.save(
                Project.builder()
                        .name(ANOTHER_PROJECT_NAME)
                        .description(ANOTHER_PROJECT_DESCRIPTION)
                        .startDate(PROJECT_START_DATE)
                        .endDate(PROJECT_END_DATE)
                        .status(Project.Status.COMPLETED)
                        .isDeleted(true)
                        .owner(user1)
                        .managers(Set.of(user1))
                        .employees(Set.of(user1))
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
        task1 = taskRepository.save(
                Task.builder()
                        .name(TASK_NAME_1)
                        .description(TASK_DESCRIPTION_1)
                        .priority(Task.Priority.LOW)
                        .status(Task.Status.NOT_STARTED)
                        .dueDate(TASK_DUE_DATE)
                        .project(livingProject)
                        .assignee(user1)
                        .isDeleted(false)
                        .build());

        livingLabel = labelRepository.save(
                        Label.builder()
                                .name(LABEL_NAME_1)
                                .color(Label.Color.RED)
                                .user(user1)
                                .tasks(Set.of(task1))
                                .build());

        labelWithNoTask = labelRepository.save(
                        Label.builder()
                                .name(LABEL_NAME_2)
                                .color(Label.Color.YELLOW)
                                .user(user1)
                                .build());
    }

    @Test
    void givenTaskAttachedToProject_whenFindAllByProjectIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByProjectIdNonDeleted(
                livingProject.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenProjectWithNoTask_whenFindAllByProjectIdNonDeleted_thenReturnEmpty() {
        Assertions.assertTrue(taskRepository
                .findAllByProjectIdNonDeleted(
                        deletedProject.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTask_whenFindAllNonDeletedWithAssigneeAndProject_thenReturnTaskAndFetchedEntities() {
        List<Task> taskList = taskRepository.findAllNonDeletedWithAssigneeAndProject();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenTask_whenFindByIdNotDeleted_thenReturnTask() {
        Task task = taskRepository.findByIdNotDeleted(task1.getId()).orElseThrow(
                () -> new EntityNotFoundException("Task with id " + task1.getId() + " not found"));
        taskAssertions(task);
    }

    @Test
    void givenDeletedByProjectTask_whenFindByIdNotDeleted_thenReturnOptionalEmpty() {
        Assertions.assertTrue(taskRepository.findByIdNotDeleted(task1.getId()).isPresent());
        taskRepository.deleteAllByProjectId(livingProject.getId());
        Assertions.assertTrue(taskRepository.findByIdNotDeleted(task1.getId()).isEmpty());
    }

    @Test
    void givenLabelAndTask_whenFindAllByLabelIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByLabelIdNonDeleted(
                livingLabel.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenLabelWithNoTask_whenFindAllByLabelIdNonDeleted_thenReturnEmptyList() {
        Assertions.assertTrue(taskRepository.findAllByLabelIdNonDeleted(
                labelWithNoTask.getId(), Pageable.unpaged()).isEmpty());
    }

    private void taskAssertions(Task task) {
        Assertions.assertNotNull(task);
        Assertions.assertEquals(task1.getId(), task.getId());
        Assertions.assertEquals(TASK_NAME_1, task.getName());
        Assertions.assertEquals(TASK_DESCRIPTION_1, task.getDescription());
        Assertions.assertEquals(Task.Priority.LOW, task.getPriority());
        Assertions.assertEquals(Task.Status.NOT_STARTED, task.getStatus());
        Assertions.assertEquals(TASK_DUE_DATE, task.getDueDate());
        Assertions.assertEquals(livingProject, task.getProject());
        Assertions.assertEquals(user1, task.getAssignee());
        Assertions.assertFalse(task.isDeleted());
    }
}
