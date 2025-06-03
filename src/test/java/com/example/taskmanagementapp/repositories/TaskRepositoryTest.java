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
    private User user1;
    private Project project1;
    private Long project2Id;
    private Long task1Id;
    private Long label1Id;
    private Long label2Id;

    @BeforeAll
    public void setUpBeforeAll() {
        Role savedRole = roleRepository.save(EntityFactory.getUserRole());
        user1 = userRepository.save(EntityFactory.getUser1(savedRole));
        project1 = projectRepository.save(EntityFactory.getProjectWithOneEmployee(user1));
        project2Id = projectRepository
                .save(EntityFactory
                        .getDeletedProject(user1))
                                                .getId();
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
        Task task1 = taskRepository.save(EntityFactory.getTask1(project1, user1));
        task1Id = task1.getId();
        Label label1 = labelRepository.save(EntityFactory.getLabel1(user1, task1));
        label1Id = label1.getId();
        Label label2 = labelRepository.save(EntityFactory.getLabelWithNoTask(user1));
        label2Id = label2.getId();
    }

    @Test
    void givenTaskAttachedToProject_whenFindAllByProjectIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByProjectIdNonDeleted(
                project1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenProjectWithNoTask_whenFindAllByProjectIdNonDeleted_thenReturnEmpty() {
        Assertions.assertTrue(taskRepository
                .findAllByProjectIdNonDeleted(project2Id, Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTask_whenFindAllNonDeletedWithAssigneeAndProject_thenReturnTaskAndFetchedEntities() {
        List<Task> taskList = taskRepository.findAllNonDeletedWithAssigneeAndProject();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenTask_whenFindByIdNotDeleted_thenReturnTask() {
        Task task = taskRepository.findByIdNotDeleted(task1Id).orElseThrow(
                () -> new EntityNotFoundException("Task with id " + task1Id + " not found"));
        taskAssertions(task);
    }

    @Test
    void givenDeletedByProjectTask_whenFindByIdNotDeleted_thenReturnOptionalEmpty() {
        Assertions.assertTrue(taskRepository.findByIdNotDeleted(task1Id).isPresent());
        taskRepository.deleteAllByProjectId(project1.getId());
        Assertions.assertTrue(taskRepository.findByIdNotDeleted(task1Id).isEmpty());
    }

    @Test
    void givenLabelAndTask_whenFindAllByLabelIdNonDeleted_thenReturnTask() {
        List<Task> taskList = taskRepository.findAllByLabelIdNonDeleted(
                label1Id, Pageable.unpaged()).getContent();
        Assertions.assertEquals(1, taskList.size());
        taskAssertions(taskList.getFirst());
    }

    @Test
    void givenLabelWithNoTask_whenFindAllByLabelIdNonDeleted_thenReturnEmptyList() {
        Assertions.assertTrue(taskRepository.findAllByLabelIdNonDeleted(
                label2Id, Pageable.unpaged()).isEmpty());
    }

    private void taskAssertions(Task task) {
        Assertions.assertNotNull(task);
        Assertions.assertEquals(task1Id, task.getId());
        Assertions.assertEquals(Constants.TASK_NAME, task.getName());
        Assertions.assertEquals(Constants.TASK_DESCRIPTION, task.getDescription());
        Assertions.assertEquals(Task.Priority.LOW, task.getPriority());
        Assertions.assertEquals(Task.Status.NOT_STARTED, task.getStatus());
        Assertions.assertEquals(Constants.TASK_DUE_DATE, task.getDueDate());
        Assertions.assertEquals(project1, task.getProject());
        Assertions.assertEquals(user1, task.getAssignee());
        Assertions.assertFalse(task.isDeleted());
    }
}
