package com.example.taskmanagementapp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entity.Attachment;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentRepositoryTest {
    private static final String TEST_USERNAME = "JohnDoe";
    private static final String TEST_PASSWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String TEST_EMAIL = "john_doe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final String TASK_NAME = "taskName";
    private static final String ANOTHER_TASK_NAME = "anotherTaskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String ANOTHER_TASK_DESCRIPTION = "anotherTaskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);
    private static final String FILE_ID = "fileId1";
    private static final String FILE_NAME = "fileName1";
    private static final String ANOTHER_FILE_ID = "fileId2";
    private static final String ANOTHER_FILE_NAME = "fileName2";
    private static final LocalDateTime UPLOADED_DATE = LocalDateTime.of(2025, 1, 6, 0, 0);
    private static final Logger logger = LogManager.getLogger(AttachmentRepositoryTest.class);
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
    private AttachmentRepository attachmentRepository;
    private Task task;
    private Task anotherTask;
    private Long firstAttachmentId;
    private Long anotherAttachmentId;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        User user = userRepository.save(
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

        anotherTask = taskRepository.save(Task.builder()
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
        Attachment firstSavedAttachment = attachmentRepository.save(
                Attachment.builder()
                        .task(task)
                        .fileId(FILE_ID)
                        .fileName(FILE_NAME)
                        .uploadDate(UPLOADED_DATE)
                        .build());
        firstAttachmentId = firstSavedAttachment.getId();

        Attachment anotherSavedAttachment = attachmentRepository.save(
                Attachment.builder()
                        .task(task)
                        .fileId(ANOTHER_FILE_ID)
                        .fileName(ANOTHER_FILE_NAME)
                        .uploadDate(UPLOADED_DATE)
                        .build());
        anotherAttachmentId = anotherSavedAttachment.getId();
    }

    @Test
    void givenTwoAttachments_whenFindAllByTaskId_thenReturnTwoAttachments() {
        List<Attachment> attachments = attachmentRepository.findAllByTaskId(task.getId());
        for (Attachment attachment : attachments) {
            if (attachment.getId().equals(firstAttachmentId)) {
                attachmentAssertions(attachment, FILE_ID, FILE_NAME);
            } else if (attachment.getId().equals(anotherAttachmentId)) {
                attachmentAssertions(attachment, ANOTHER_FILE_ID, ANOTHER_FILE_NAME);
            }
        }
    }

    @Test
    void givenTaskWithNoAttachment_whenFindAllByTaskId_thenReturnEmptyList() {
        assertTrue(attachmentRepository.findAllByTaskId(anotherTask.getId()).isEmpty());
    }

    private void attachmentAssertions(Attachment attachment, String fileId, String fileName) {
        assertEquals(fileId, attachment.getFileId());
        assertEquals(fileName, attachment.getFileName());
        assertEquals(task, attachment.getTask());
        assertEquals(UPLOADED_DATE, attachment.getUploadDate());
    }
}
