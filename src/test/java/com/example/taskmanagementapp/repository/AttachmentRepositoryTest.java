package com.example.taskmanagementapp.repository;

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
import org.junit.jupiter.api.Assertions;
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
    private static final String USERNAME_1 = "JohnDoe";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final String TASK_NAME_1 = "taskName";
    private static final String TASK_NAME_2 = "anotherTaskName";
    private static final String TASK_DESCRIPTION_1 = "taskDescription";
    private static final String TASK_DESCRIPTION_2 = "anotherTaskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);
    private static final String FILE_ID_1 = "fileId1";
    private static final String FILE_NAME_1 = "fileName1";
    private static final String FILE_ID_2 = "fileId2";
    private static final String FILE_NAME_2 = "fileName2";
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
    private Task task1;
    private Task task2;
    private Long attachment1Id;
    private Long attachment2Id;

    @BeforeAll
    void setUpBeforeAll() {
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        User user1 = userRepository.save(
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
        Attachment attachment1 = attachmentRepository.save(
                Attachment.builder()
                        .task(task1)
                        .fileId(FILE_ID_1)
                        .fileName(FILE_NAME_1)
                        .uploadDate(UPLOADED_DATE)
                        .build());
        attachment1Id = attachment1.getId();

        Attachment attachment2 = attachmentRepository.save(
                Attachment.builder()
                        .task(task1)
                        .fileId(FILE_ID_2)
                        .fileName(FILE_NAME_2)
                        .uploadDate(UPLOADED_DATE)
                        .build());
        attachment2Id = attachment2.getId();
    }

    @Test
    void givenTwoAttachments_whenFindAllByTaskId_thenReturnTwoAttachments() {
        List<Attachment> attachments = attachmentRepository.findAllByTaskId(task1.getId());
        for (Attachment attachment : attachments) {
            if (attachment.getId().equals(attachment1Id)) {
                attachmentAssertions(attachment, FILE_ID_1, FILE_NAME_1);
            } else if (attachment.getId().equals(attachment2Id)) {
                attachmentAssertions(attachment, FILE_ID_2, FILE_NAME_2);
            }
        }
    }

    @Test
    void givenTaskWithNoAttachment_whenFindAllByTaskId_thenReturnEmptyList() {
        Assertions.assertTrue(attachmentRepository.findAllByTaskId(task2.getId()).isEmpty());
    }

    private void attachmentAssertions(Attachment attachment, String fileId, String fileName) {
        Assertions.assertEquals(fileId, attachment.getFileId());
        Assertions.assertEquals(fileName, attachment.getFileName());
        Assertions.assertEquals(task1, attachment.getTask());
        Assertions.assertEquals(UPLOADED_DATE, attachment.getUploadDate());
    }
}
