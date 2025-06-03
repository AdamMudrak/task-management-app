package com.example.taskmanagementapp.repositories;

import com.dropbox.core.v2.DbxClientV2;
import com.example.taskmanagementapp.entities.Attachment;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentRepositoryTest {
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
        Role savedRole = roleRepository.save(EntityFactory.getUserRole());
        User user1 = userRepository.save(EntityFactory.getUser1(savedRole));
        Project project =
                projectRepository.save(EntityFactory.getProjectWithOneEmployee(user1));
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
        Attachment attachment1 = attachmentRepository.save(EntityFactory.getAttachment1(task1));
        attachment1Id = attachment1.getId();

        Attachment attachment2 = attachmentRepository.save(EntityFactory.getAttachment2(task1));
        attachment2Id = attachment2.getId();
    }

    @Test
    void givenTwoAttachments_whenFindAllByTaskId_thenReturnTwoAttachments() {
        List<Attachment> attachments = attachmentRepository.findAllByTaskId(task1.getId());
        for (Attachment attachment : attachments) {
            if (attachment.getId().equals(attachment1Id)) {
                attachmentAssertions(attachment, Constants.FILE_ID_1, Constants.FILE_NAME_1);
            } else if (attachment.getId().equals(attachment2Id)) {
                attachmentAssertions(attachment, Constants.FILE_ID_2, Constants.FILE_NAME_2);
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
        Assertions.assertEquals(Constants.UPLOADED_DATE, attachment.getUploadDate());
    }
}
