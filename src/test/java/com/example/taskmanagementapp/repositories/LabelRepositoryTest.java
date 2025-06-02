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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LabelRepositoryTest {
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
    private User testUser;
    private User anotherTestUser;
    private Task testTask;
    private Task anotherTestTask;
    private Long labelId;
    private Long anotherLabelId;

    @BeforeAll
    void setUpBeforeAll() {
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

        User user2 = new User();
        user2.setUsername(Constants.ANOTHER_USERNAME);
        user2.setPassword(Constants.PASSWORD);
        user2.setEmail(Constants.ANOTHER_EMAIL);
        user2.setFirstName(Constants.FIRST_NAME);
        user2.setLastName(Constants.LAST_NAME);
        user2.setRole(savedRole);
        user2.setEnabled(true);
        user2.setAccountNonLocked(true);
        anotherTestUser = userRepository.save(user2);

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
        project.getEmployees().add(user2);
        projectRepository.save(project);

        Task task = new Task();
        task.setName(Constants.TASK_NAME);
        task.setDescription(Constants.TASK_DESCRIPTION);
        task.setPriority(Task.Priority.LOW);
        task.setStatus(Task.Status.NOT_STARTED);
        task.setDueDate(Constants.TASK_DUE_DATE);
        task.setProject(project);
        task.setAssignee(user);
        task.setDeleted(false);
        testTask = taskRepository.save(task);

        Task anotherTask = new Task();
        anotherTask.setName(Constants.ANOTHER_TASK_NAME);
        anotherTask.setDescription(Constants.ANOTHER_TASK_DESCRIPTION);
        anotherTask.setPriority(Task.Priority.HIGH);
        anotherTask.setStatus(Task.Status.IN_PROGRESS);
        anotherTask.setDueDate(Constants.TASK_DUE_DATE);
        anotherTask.setProject(project);
        anotherTask.setAssignee(user);
        anotherTask.setDeleted(false);
        anotherTestTask = taskRepository.save(anotherTask);
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
    public void setUp() {
        Label label = new Label();
        label.setName(Constants.LABEL_NAME);
        label.setUser(testUser);
        label.setColor(Label.Color.GREEN);
        label.getTasks().add(testTask);
        labelId = labelRepository.save(label).getId();

        Label anotherLabel = new Label();
        anotherLabel.setName(Constants.ANOTHER_LABEL_NAME);
        anotherLabel.setUser(testUser);
        anotherLabel.setColor(Label.Color.RED);
        anotherLabel.getTasks().add(anotherTestTask);
        anotherLabelId = labelRepository.save(anotherLabel).getId();
    }

    @Test
    void givenTwoLabels_whenFindByIdAndUserId_thenReturnBoth() {
        Label label = labelRepository.findByIdAndUserId(labelId, testUser.getId()).orElseThrow(
                () -> new EntityNotFoundException("Label with id " + labelId + " not found"));
        labelAssertions(label, labelId, Constants.LABEL_NAME, Label.Color.GREEN, testTask);

        Label anotherLabel = labelRepository.findByIdAndUserId(anotherLabelId, testUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Label with id " + anotherLabelId + " not found"));
        labelAssertions(anotherLabel, anotherLabelId, Constants.ANOTHER_LABEL_NAME,
                Label.Color.RED, anotherTestTask);
    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenFindByIdAndUserId_thenReturnEmpty() {
        Assertions.assertTrue(labelRepository.findByIdAndUserId(
                labelId, anotherTestUser.getId()).isEmpty());
    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenFindAllByUserId_thenReturnEmpty() {
        Assertions.assertTrue(labelRepository
                .findAllByUserId(anotherTestUser.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTwoLabelsAndUserWithLabels_whenExistsByIdAndUserId_thenReturnTrue() {
        Assertions.assertTrue(labelRepository
                .existsByIdAndUserId(labelId, testUser.getId()));
        Assertions.assertTrue(labelRepository
                .existsByIdAndUserId(anotherLabelId, testUser.getId()));

    }

    @Test
    void givenTwoLabelsAndUserWithNoLabels_whenExistsByIdAndUserId_thenReturnFalse() {
        Assertions.assertFalse(labelRepository
                .existsByIdAndUserId(labelId, anotherTestUser.getId()));
        Assertions.assertFalse(labelRepository
                .existsByIdAndUserId(anotherLabelId, anotherTestUser.getId()));

    }

    @Test
    void givenTwoLabels_whenFindAllByUserId_thenReturnBoth() {
        List<Label> labels = labelRepository.findAllByUserId(
                testUser.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(2, labels.size());

        for (Label label : labels) {
            if (label.getId().equals(labelId)) {
                labelAssertions(label, labelId, Constants.LABEL_NAME, Label.Color.GREEN, testTask);
            } else if (label.getId().equals(anotherLabelId)) {
                labelAssertions(label, anotherLabelId, Constants.ANOTHER_LABEL_NAME,
                        Label.Color.RED, anotherTestTask);
            }
        }
    }

    @Test
    void givenTwoLabels_whenFindAllByUserIdWithPageable_thenReturnSingleResult() {
        int firstPage = 0;
        int secondPage = 1;
        int size = 1;

        List<Label> listOfFirstLabel = labelRepository.findAllByUserId(
                testUser.getId(), PageRequest.of(firstPage, size)).getContent();
        Assertions.assertEquals(1, listOfFirstLabel.size());
        labelAssertions(listOfFirstLabel.getFirst(), labelId,
                Constants.LABEL_NAME, Label.Color.GREEN, testTask);

        List<Label> listOfSecondLabel = labelRepository.findAllByUserId(
                testUser.getId(), PageRequest.of(secondPage, size)).getContent();
        Assertions.assertEquals(1, listOfSecondLabel.size());
        labelAssertions(listOfSecondLabel.getFirst(), anotherLabelId, Constants.ANOTHER_LABEL_NAME,
                Label.Color.RED, anotherTestTask);
    }

    private void labelAssertions(Label label,Long id, String name, Label.Color color, Task task) {
        Assertions.assertNotNull(label);
        Assertions.assertEquals(id, label.getId());
        Assertions.assertEquals(name, label.getName());
        Assertions.assertEquals(color, label.getColor());
        Assertions.assertEquals(testUser, label.getUser());
        Assertions.assertEquals(1, label.getTasks().size());
        Assertions.assertTrue(label.getTasks().contains(task));
    }
}
