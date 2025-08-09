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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LabelRepositoryTest {
    private static final String USERNAME_1 = "JohnDoe";
    private static final String USERNAME_2 = "RichardRoe";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";
    private static final String EMAIL_2 = "richard_roe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String ANOTHER_FIRST_NAME = "Richard";
    private static final String ANOTHER_LAST_NAME = "Roe";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final String TASK_NAME_1 = "taskName";
    private static final String TASK_NAME_2 = "anotherTaskName";
    private static final String TASK_DESCRIPTION_1 = "taskDescription";
    private static final String TASK_DESCRIPTION_2 = "anotherTaskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);
    private static final String LABEL_NAME_1 = "labelName";
    private static final String LABEL_NAME_2 = "anotherLabelName";
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
    private User user1;
    private User user2;
    private Task task1;
    private Task task2;
    private Long labelId1;
    private Long labelId2;

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

        user2 = userRepository.save(
                User.builder()
                        .username(USERNAME_2)
                        .password(PASSWORD_1_DB)
                        .email(EMAIL_2)
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
                        .owner(user1)
                        .managers(Set.of(user1))
                        .employees(Set.of(user1))
                        .build());

        task1 = taskRepository.save(
                Task.builder()
                        .name(TASK_NAME_1)
                        .description(TASK_DESCRIPTION_1)
                        .priority(Task.Priority.LOW)
                        .status(Task.Status.NOT_STARTED)
                        .dueDate(TASK_DUE_DATE)
                        .project(project)
                        .assignee(user1)
                        .isDeleted(false)
                        .build());

        task2 = taskRepository.save(Task.builder()
                .name(TASK_NAME_2)
                .description(TASK_DESCRIPTION_2)
                .priority(Task.Priority.MEDIUM)
                .status(Task.Status.IN_PROGRESS)
                .dueDate(TASK_DUE_DATE)
                .project(project)
                .assignee(user1)
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
        labelId1 = labelRepository.save(
                Label.builder()
                        .name(LABEL_NAME_1)
                        .color(Label.Color.RED)
                        .user(user1)
                        .tasks(Set.of(task1))
                        .build())
                .getId();
        labelId2 = labelRepository.save(
                Label.builder()
                        .name(LABEL_NAME_2)
                        .color(Label.Color.YELLOW)
                        .user(user1)
                        .tasks(Set.of(task2))
                        .build())
                .getId();
    }

    @Test
    void givenTwoLabels_whenFindByIdAndUserId_thenReturnBoth() {
        Label label = labelRepository.findByIdAndUserId(labelId1, user1.getId()).orElseThrow(
                () -> new EntityNotFoundException("Label with id " + labelId1 + " not found"));
        labelAssertions(label, labelId1, LABEL_NAME_1, Label.Color.RED, task1);

        Label anotherLabel = labelRepository.findByIdAndUserId(labelId2, user1.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Label with id " + labelId2 + " not found"));
        labelAssertions(anotherLabel, labelId2, LABEL_NAME_2,
                Label.Color.YELLOW, task2);
    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenFindByIdAndUserId_thenReturnEmpty() {
        Assertions.assertTrue(labelRepository.findByIdAndUserId(
                labelId1, user2.getId()).isEmpty());
    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenFindAllByUserId_thenReturnEmpty() {
        Assertions.assertTrue(labelRepository
                .findAllByUserId(user2.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTwoLabelsAndUserWithLabels_whenExistsByIdAndUserId_thenReturnTrue() {
        Assertions.assertTrue(labelRepository
                .existsByIdAndUserId(labelId1, user1.getId()));
        Assertions.assertTrue(labelRepository
                .existsByIdAndUserId(labelId2, user1.getId()));

    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenExistsByIdAndUserId_thenReturnFalse() {
        Assertions.assertFalse(labelRepository
                .existsByIdAndUserId(labelId1, user2.getId()));
        Assertions.assertFalse(labelRepository
                .existsByIdAndUserId(labelId2, user2.getId()));

    }

    @Test
    void givenTwoLabels_whenFindAllByUserId_thenReturnBoth() {
        List<Label> labels = labelRepository.findAllByUserId(
                user1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(2, labels.size());

        for (Label label : labels) {
            if (label.getId().equals(labelId1)) {
                labelAssertions(label, labelId1, LABEL_NAME_1, Label.Color.RED, task1);
            } else if (label.getId().equals(labelId2)) {
                labelAssertions(label, labelId2, LABEL_NAME_2,
                        Label.Color.YELLOW, task2);
            }
        }
    }

    @Test
    void givenTwoLabels_whenFindAllByUserIdWithPageable_thenReturnSingleResult() {
        int firstPage = 0;
        int secondPage = 1;
        int size = 1;

        List<Label> listOfFirstLabel = labelRepository.findAllByUserId(
                user1.getId(), PageRequest.of(firstPage, size)).getContent();
        Assertions.assertEquals(1, listOfFirstLabel.size());
        labelAssertions(listOfFirstLabel.getFirst(), labelId1,
                LABEL_NAME_1, Label.Color.RED, task1);

        List<Label> listOfSecondLabel = labelRepository.findAllByUserId(
                user1.getId(), PageRequest.of(secondPage, size)).getContent();
        Assertions.assertEquals(1, listOfSecondLabel.size());
        labelAssertions(listOfSecondLabel.getFirst(), labelId2, LABEL_NAME_2,
                Label.Color.YELLOW, task2);
    }

    private void labelAssertions(Label label,Long id, String name, Label.Color color, Task task) {
        Assertions.assertNotNull(label);
        Assertions.assertEquals(id, label.getId());
        Assertions.assertEquals(name, label.getName());
        Assertions.assertEquals(color, label.getColor());
        Assertions.assertEquals(user1, label.getUser());
        Assertions.assertEquals(1, label.getTasks().size());
        Assertions.assertTrue(label.getTasks().contains(task));
    }
}
