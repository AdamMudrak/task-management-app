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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabelRepositoryTest {
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
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final String TASK_NAME = "taskName";
    private static final String ANOTHER_TASK_NAME = "anotherTaskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String ANOTHER_TASK_DESCRIPTION = "anotherTaskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);
    private static final String LABEL_NAME = "labelName";
    private static final String ANOTHER_LABEL_NAME = "anotherLabelName";
    private static final Logger logger = LogManager.getLogger(LabelRepositoryTest.class);
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
    private User anotherSavedUser;
    private Task savedTask;
    private Task anotherSavedTask;
    private Long labelId;
    private Long anotherLabelId;

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

        Project project = projectRepository.save(
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
                        .build());

        savedTask = taskRepository.save(
                Task.builder()
                        .name(TASK_NAME)
                        .description(TASK_DESCRIPTION)
                        .priority(Task.Priority.LOW)
                        .status(Task.Status.NOT_STARTED)
                        .dueDate(TASK_DUE_DATE)
                        .project(project)
                        .assignee(savedUser)
                        .isDeleted(false)
                        .build());

        anotherSavedTask = taskRepository.save(Task.builder()
                .name(ANOTHER_TASK_NAME)
                .description(ANOTHER_TASK_DESCRIPTION)
                .priority(Task.Priority.MEDIUM)
                .status(Task.Status.IN_PROGRESS)
                .dueDate(TASK_DUE_DATE)
                .project(project)
                .assignee(savedUser)
                .isDeleted(false)
                .build());
    }

    /**Project, Task, User and Role related tables have to be cleaned manually via sql
     *because of the Project and Task entity soft delete logic
     */
    @SuppressWarnings("'Delete' statement without 'where' clears all data in the table")
    @AfterAll
    void tearDownAfterAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM tasks");
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
        labelId = labelRepository.save(
                Label.builder()
                        .name(LABEL_NAME)
                        .color(Label.Color.RED)
                        .user(savedUser)
                        .tasks(Set.of(savedTask))
                        .build())
                .getId();
        anotherLabelId = labelRepository.save(
                Label.builder()
                        .name(ANOTHER_LABEL_NAME)
                        .color(Label.Color.YELLOW)
                        .user(savedUser)
                        .tasks(Set.of(anotherSavedTask))
                        .build())
                .getId();
    }

    @Test
    void givenTwoLabels_whenFindByIdAndUserId_thenReturnBoth() {
        Label label = labelRepository.findByIdAndUserId(labelId, savedUser.getId()).orElseThrow(
                () -> new EntityNotFoundException("Label with id " + labelId + " not found"));
        labelAssertions(label, labelId, LABEL_NAME, Label.Color.RED, savedTask);

        Label anotherLabel = labelRepository.findByIdAndUserId(anotherLabelId, savedUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Label with id " + anotherLabelId + " not found"));
        labelAssertions(anotherLabel, anotherLabelId, ANOTHER_LABEL_NAME,
                Label.Color.YELLOW, anotherSavedTask);
    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenFindByIdAndUserId_thenReturnEmpty() {
        assertTrue(labelRepository.findByIdAndUserId(
                labelId, anotherSavedUser.getId()).isEmpty());
    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenFindAllByUserId_thenReturnEmpty() {
        assertTrue(labelRepository
                .findAllByUserId(anotherSavedUser.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTwoLabelsAndUserWithLabels_whenExistsByIdAndUserId_thenReturnTrue() {
        assertTrue(labelRepository
                .existsByIdAndUserId(labelId, savedUser.getId()));
        assertTrue(labelRepository
                .existsByIdAndUserId(anotherLabelId, savedUser.getId()));

    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenExistsByIdAndUserId_thenReturnFalse() {
        assertFalse(labelRepository
                .existsByIdAndUserId(labelId, anotherSavedUser.getId()));
        assertFalse(labelRepository
                .existsByIdAndUserId(anotherLabelId, anotherSavedUser.getId()));

    }

    @Test
    void givenTwoLabels_whenFindAllByUserId_thenReturnBoth() {
        List<Label> labels = labelRepository.findAllByUserId(
                savedUser.getId(), Pageable.unpaged()).getContent();
        assertEquals(2, labels.size());

        for (Label label : labels) {
            if (label.getId().equals(labelId)) {
                labelAssertions(label, labelId, LABEL_NAME, Label.Color.RED, savedTask);
            } else if (label.getId().equals(anotherLabelId)) {
                labelAssertions(label, anotherLabelId, ANOTHER_LABEL_NAME,
                        Label.Color.YELLOW, anotherSavedTask);
            }
        }
    }

    @Test
    void givenTwoLabels_whenFindAllByUserIdWithPageable_thenReturnSingleResult() {
        int firstPage = 0;
        int secondPage = 1;
        int size = 1;

        List<Label> listOfFirstLabel = labelRepository.findAllByUserId(
                savedUser.getId(), PageRequest.of(firstPage, size)).getContent();
        assertEquals(1, listOfFirstLabel.size());
        labelAssertions(listOfFirstLabel.getFirst(), labelId,
                LABEL_NAME, Label.Color.RED, savedTask);

        List<Label> listOfSecondLabel = labelRepository.findAllByUserId(
                savedUser.getId(), PageRequest.of(secondPage, size)).getContent();
        assertEquals(1, listOfSecondLabel.size());
        labelAssertions(listOfSecondLabel.getFirst(), anotherLabelId, ANOTHER_LABEL_NAME,
                Label.Color.YELLOW, anotherSavedTask);
    }

    private void labelAssertions(Label label,Long id, String name, Label.Color color, Task task) {
        assertNotNull(label);
        assertEquals(id, label.getId());
        assertEquals(name, label.getName());
        assertEquals(color, label.getColor());
        assertEquals(savedUser, label.getUser());
        assertEquals(1, label.getTasks().size());
        assertTrue(label.getTasks().contains(task));
    }
}
