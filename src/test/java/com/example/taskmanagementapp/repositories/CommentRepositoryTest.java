package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Comment;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.EntityFactory;
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
public class CommentRepositoryTest {
    private static final Logger logger = LogManager.getLogger(CommentRepositoryTest.class);
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
    private CommentRepository commentRepository;
    private User user1;
    private User user2;
    private Task task1;
    private Task task2;
    private Comment comment1;
    private Comment comment2;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(EntityFactory.getUserRole());
        user1 = userRepository.save(EntityFactory.getUser1(savedRole));
        user2 = userRepository.save(EntityFactory.getUser2(savedRole));
        Project project =
                projectRepository.save(EntityFactory.getProjectWithTwoEmployees(user1, user2));
        task1 = taskRepository.save(EntityFactory.getTask1(project, user1));
        task2 = taskRepository.save(EntityFactory.getTask2(project, user1));
    }

    /**Task, Project, User and Role related tables have to be cleaned manually via sql
     *because of the Project entity soft delete logic
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
        comment1 = commentRepository.save(EntityFactory.getComment1(user1, task1));
        comment2 = commentRepository.save(EntityFactory.getComment2(user1, task1));
    }

    @Test
    void givenTwoCommentsOfUser1ForTask1_whenFindAllByTaskId_thenReturnListOfTwoComments() {
        List<Comment> commentList =
                commentRepository.findAllByTaskId(task1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(2, commentList.size());
        for (Comment comment : commentList) {
            if (comment.getId().equals(comment1.getId())) {
                commentAssertions(comment, task1, user1,
                        Constants.COMMENT_TEXT_1);
            } else if (comment.getId().equals(comment2.getId())) {
                commentAssertions(comment, task1, user1,
                        Constants.COMMENT_TEXT_2);
            }
        }
    }

    @Test
    void givenTaskWithNoComments_whenFindAllByTaskId_thenReturnEmptyList() {
        Assertions.assertTrue(
                commentRepository.findAllByTaskId(task2.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTwoCommentsOfUser1ForTask1_whenDeleteAllByTaskId_thenReturnEmptyList() {
        List<Comment> commentList =
                commentRepository.findAllByTaskId(task1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(2, commentList.size());
        commentRepository.deleteAllByTaskId(task1.getId());
        Assertions.assertTrue(
                commentRepository.findAllByTaskId(task1.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenComment_whenFindByIdAndUserId_thenReturnCommentTwice() {
        Comment firstComment = commentRepository.findByIdAndUserId(comment1.getId(), user1.getId())
                .orElseThrow(() -> new EntityNotFoundException("No comment with id "
                        + comment1.getId() + " for user with id " + user1.getId()));
        commentAssertions(firstComment, task1, user1, Constants.COMMENT_TEXT_1);

        Comment secondComment = commentRepository.findByIdAndUserId(comment2.getId(), user1.getId())
                .orElseThrow(() -> new EntityNotFoundException("No comment with id "
                        + comment2.getId() + " for user with id " + user1.getId()));
        commentAssertions(secondComment, task1, user1, Constants.COMMENT_TEXT_2);
    }

    @Test
    void givenUserWithNoComment_whenFindByIdAndUserId_thenReturnEmpty() {
        Assertions.assertTrue(
                commentRepository.findByIdAndUserId(comment1.getId(), user2.getId()).isEmpty());
        Assertions.assertTrue(
                commentRepository.findByIdAndUserId(comment2.getId(), user2.getId()).isEmpty());
    }

    private void commentAssertions(Comment comment, Task task, User user,
                                   String commentText) {
        Assertions.assertEquals(task, comment.getTask());
        Assertions.assertEquals(user, comment.getUser());
        Assertions.assertEquals(commentText, comment.getText());
        Assertions.assertEquals(Constants.TIME_STAMP, comment.getTimestamp());
    }
}
