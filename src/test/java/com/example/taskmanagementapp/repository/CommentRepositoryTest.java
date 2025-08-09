package com.example.taskmanagementapp.repository;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.Comment;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class CommentRepositoryTest {
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
    private static final LocalDateTime TIME_STAMP = LocalDateTime.of(2025, 1, 6, 8, 30);
    private static final String COMMENT_TEXT_1 = "commentText1";
    private static final String COMMENT_TEXT_2 = "commentText2";
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
        comment1 = commentRepository.save(
                Comment.builder()
                        .task(task1)
                        .user(user1)
                        .text(COMMENT_TEXT_1)
                        .timestamp(TIME_STAMP)
                        .build());

        comment2 = commentRepository.save(
                Comment.builder()
                        .task(task1)
                        .user(user1)
                        .text(COMMENT_TEXT_2)
                        .timestamp(TIME_STAMP)
                        .build());
    }

    @Test
    void givenTwoCommentsOfUser1ForTask1_whenFindAllByTaskId_thenReturnListOfTwoComments() {
        List<Comment> commentList =
                commentRepository.findAllByTaskId(task1.getId(), Pageable.unpaged()).getContent();
        Assertions.assertEquals(2, commentList.size());
        for (Comment comment : commentList) {
            if (comment.getId().equals(comment1.getId())) {
                commentAssertions(comment, task1, user1,
                        COMMENT_TEXT_1);
            } else if (comment.getId().equals(comment2.getId())) {
                commentAssertions(comment, task1, user1,
                        COMMENT_TEXT_2);
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
        commentAssertions(firstComment, task1, user1, COMMENT_TEXT_1);

        Comment secondComment = commentRepository.findByIdAndUserId(comment2.getId(), user1.getId())
                .orElseThrow(() -> new EntityNotFoundException("No comment with id "
                        + comment2.getId() + " for user with id " + user1.getId()));
        commentAssertions(secondComment, task1, user1, COMMENT_TEXT_2);
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
        Assertions.assertEquals(TIME_STAMP, comment.getTimestamp());
    }
}
