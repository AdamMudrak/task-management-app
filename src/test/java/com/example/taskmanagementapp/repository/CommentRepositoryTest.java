package com.example.taskmanagementapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private static final LocalDateTime TIME_STAMP = LocalDateTime.of(2025, 1, 6, 8, 30);
    private static final String COMMENT_TEXT = "commentText1";
    private static final String ANOTHER_COMMENT_TEXT = "commentText2";
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
    private User user;
    private User userWithNoComments;
    private Task task;
    private Task taskWithNoComments;
    private Comment firstTaskComment;
    private Comment anotherTaskComment;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        user = userRepository.save(
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

        userWithNoComments = userRepository.save(
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
                        .owner(user)
                        .managers(Set.of(user))
                        .employees(Set.of(user))
                        .build());

        task = taskRepository.save(
                Task.builder()
                        .name(TASK_NAME)
                        .description(TASK_DESCRIPTION)
                        .priority(Task.Priority.LOW)
                        .status(Task.Status.NOT_STARTED)
                        .dueDate(TASK_DUE_DATE)
                        .project(project)
                        .assignee(user)
                        .isDeleted(false)
                        .build());

        taskWithNoComments = taskRepository.save(Task.builder()
                .name(ANOTHER_TASK_NAME)
                .description(ANOTHER_TASK_DESCRIPTION)
                .priority(Task.Priority.MEDIUM)
                .status(Task.Status.IN_PROGRESS)
                .dueDate(TASK_DUE_DATE)
                .project(project)
                .assignee(user)
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
        firstTaskComment = commentRepository.save(
                Comment.builder()
                        .task(task)
                        .user(user)
                        .text(COMMENT_TEXT)
                        .timestamp(TIME_STAMP)
                        .build());

        anotherTaskComment = commentRepository.save(
                Comment.builder()
                        .task(task)
                        .user(user)
                        .text(ANOTHER_COMMENT_TEXT)
                        .timestamp(TIME_STAMP)
                        .build());
    }

    @Test
    void givenTwoCommentsOfUserForTask_whenFindAllByTaskId_thenReturnListOfTwoComments() {
        List<Comment> commentList =
                commentRepository.findAllByTaskId(task.getId(), Pageable.unpaged()).getContent();
        assertEquals(2, commentList.size());
        for (Comment comment : commentList) {
            if (comment.getId().equals(firstTaskComment.getId())) {
                commentAssertions(comment, task, user,
                        COMMENT_TEXT);
            } else if (comment.getId().equals(anotherTaskComment.getId())) {
                commentAssertions(comment, task, user,
                        ANOTHER_COMMENT_TEXT);
            }
        }
    }

    @Test
    void givenTaskWithNoComments_whenFindAllByTaskId_thenReturnEmptyList() {
        assertTrue(
                commentRepository.findAllByTaskId(taskWithNoComments.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenTwoCommentsOfUserForTask_whenDeleteAllByTaskId_thenReturnEmptyList() {
        List<Comment> commentList =
                commentRepository.findAllByTaskId(task.getId(), Pageable.unpaged()).getContent();
        assertEquals(2, commentList.size());
        commentRepository.deleteAllByTaskId(task.getId());
        assertTrue(
                commentRepository.findAllByTaskId(task.getId(), Pageable.unpaged()).isEmpty());
    }

    @Test
    void givenComment_whenFindByIdAndUserId_thenReturnCommentTwice() {
        Comment firstComment = commentRepository.findByIdAndUserId(firstTaskComment.getId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("No comment with id "
                        + firstTaskComment.getId() + " for user with id " + user.getId()));
        commentAssertions(firstComment, task, user, COMMENT_TEXT);

        Comment secondComment = commentRepository.findByIdAndUserId(anotherTaskComment.getId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("No comment with id "
                        + anotherTaskComment.getId() + " for user with id " + user.getId()));
        commentAssertions(secondComment, task, user, ANOTHER_COMMENT_TEXT);
    }

    @Test
    void givenUserWithNoComment_whenFindByIdAndUserId_thenReturnEmpty() {
        assertTrue(
                commentRepository.findByIdAndUserId(firstTaskComment.getId(), userWithNoComments.getId()).isEmpty());
        assertTrue(
                commentRepository.findByIdAndUserId(anotherTaskComment.getId(), userWithNoComments.getId()).isEmpty());
    }

    private void commentAssertions(Comment comment, Task task, User user,
                                   String commentText) {
        assertEquals(task, comment.getTask());
        assertEquals(user, comment.getUser());
        assertEquals(commentText, comment.getText());
        assertEquals(TIME_STAMP, comment.getTimestamp());
    }
}
