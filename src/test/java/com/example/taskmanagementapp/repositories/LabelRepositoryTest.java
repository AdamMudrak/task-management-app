package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.EntityFactory;
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
    private User user1;
    private User user2;
    private Task task1;
    private Task task2;
    private Long labelId1;
    private Long labelId2;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(EntityFactory.getUserRole());
        user1 = userRepository.save(EntityFactory.getUser1(savedRole));
        user2 = userRepository.save(EntityFactory.getUser2(savedRole));
        Project project =
                projectRepository.save(EntityFactory.getProjectWithTwoEmployees(user1, user2));
        task1 = taskRepository.save(EntityFactory.getTask1(project, user1));
        task2 = taskRepository.save(EntityFactory.getTask2(project, user2));
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
        labelId1 = labelRepository.save(EntityFactory.getLabel1(user1, task1)).getId();
        labelId2 = labelRepository.save(EntityFactory.getLabel2(user1, task2)).getId();
    }

    @Test
    void givenTwoLabels_whenFindByIdAndUserId_thenReturnBoth() {
        Label label = labelRepository.findByIdAndUserId(labelId1, user1.getId()).orElseThrow(
                () -> new EntityNotFoundException("Label with id " + labelId1 + " not found"));
        labelAssertions(label, labelId1, Constants.LABEL_NAME, Label.Color.GREEN, task1);

        Label anotherLabel = labelRepository.findByIdAndUserId(labelId2, user1.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Label with id " + labelId2 + " not found"));
        labelAssertions(anotherLabel, labelId2, Constants.ANOTHER_LABEL_NAME,
                Label.Color.RED, task2);
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
                labelAssertions(label, labelId1, Constants.LABEL_NAME, Label.Color.GREEN, task1);
            } else if (label.getId().equals(labelId2)) {
                labelAssertions(label, labelId2, Constants.ANOTHER_LABEL_NAME,
                        Label.Color.RED, task2);
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
                Constants.LABEL_NAME, Label.Color.GREEN, task1);

        List<Label> listOfSecondLabel = labelRepository.findAllByUserId(
                user1.getId(), PageRequest.of(secondPage, size)).getContent();
        Assertions.assertEquals(1, listOfSecondLabel.size());
        labelAssertions(listOfSecondLabel.getFirst(), labelId2, Constants.ANOTHER_LABEL_NAME,
                Label.Color.RED, task2);
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
