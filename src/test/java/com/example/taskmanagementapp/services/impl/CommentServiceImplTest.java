package com.example.taskmanagementapp.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.comment.request.CommentRequest;
import com.example.taskmanagementapp.dtos.comment.request.UpdateCommentRequest;
import com.example.taskmanagementapp.dtos.comment.response.CommentResponse;
import com.example.taskmanagementapp.entities.Comment;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.CommentMapper;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.services.utils.ProjectAuthorityUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
    public static final String USERNAME_1 = "JohnDoe";
    public static final String PASSWORD_1 = "Best_Password1@3$";
    public static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    public static final String EMAIL_1 = "john_doe@mail.com";

    public static final String USERNAME_2 = "RichardRoe";
    public static final String PASSWORD_2 = "newPassword1@";
    public static final String EMAIL_2 = "richard_roe@mail.com";

    public static final String USERNAME_3 = "JaneDoe";
    public static final String PASSWORD_3 = "newPassword2@";
    public static final String EMAIL_3 = "jane_doe@mail.com";

    public static final String USERNAME_4 = "RickyRoe";
    public static final String EMAIL_4 = "ricky_roe@mail.com";

    public static final String USERNAME_5 = "TheBestJohnDoe";
    public static final String EMAIL_5 = "bestjohndoe@mail.com";

    public static final String USERNAME_6 = "TheNewJohnDoe";
    public static final String EMAIL_6 = "newjohndoe@mail.com";

    public static final String USERNAME_7 = "YetAnotherJohnDoe";
    public static final String EMAIL_7 = "yetanothertestjohndoe@mail.com";

    public static final String INVALID_USERNAME = "username@likemail.com";
    public static final String INVALID_EMAIL = "invalidmail.com";
    public static final String INVALID_PASSWORD = "password";
    public static final String ANOTHER_INVALID_PASSWORD = "new_password";
    public static final String EMPTY = "";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";

    public static final String ANOTHER_FIRST_NAME = "Richard";
    public static final String ANOTHER_LAST_NAME = "Roe";

    public static final String ACTION_TOKEN = "actionToken";
    public static final String NOT_EXISTING_ACTION_TOKEN = "blaBlaBlaActionToken";

    public static final String PROJECT_NAME = "projectName";
    public static final String PROJECT_DESCRIPTION = "projectDescription";
    public static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    public static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    public static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    public static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);

    public static final Long FIRST_TASK_ID = 1L;
    public static final Long SECOND_TASK_ID = 2L;
    public static final String TASK_NAME_1 = "taskName";
    public static final String TASK_NAME_2 = "anotherTaskName";
    public static final String TASK_DESCRIPTION_1 = "taskDescription";
    public static final String TASK_DESCRIPTION_2 = "anotherTaskDescription";
    public static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);

    public static final String LABEL_NAME_1 = "labelName";
    public static final String LABEL_NAME_2 = "anotherLabelName";

    public static final String FILE_ID_1 = "fileId1";
    public static final String FILE_NAME_1 = "fileName1";
    public static final String FILE_ID_2 = "fileId2";
    public static final String FILE_NAME_2 = "fileName2";
    public static final LocalDateTime UPLOADED_DATE = LocalDateTime.of(2025, 1, 6, 0, 0);

    public static final LocalDateTime TIME_STAMP = LocalDateTime.of(2025, 1, 6, 8, 30);

    public static final List<String> EXPECTED_ERRORS_ON_REGISTER = List.of(
            "firstName must not be blank.",
            "lastName must not be blank.",
            "password and repeatPassword don't match. Try again.",
            "password  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "repeatPassword  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "email : invalid email. Try again.",
            "username : invalid username. Can't be like email.");

    public static final long ULTRA_SHORT_EXPIRATION = 1L;
    public static final long ACTION_EXPIRATION = 60000L;
    public static final long ACCESS_EXPIRATION = 900000L;
    public static final long REFRESH_EXPIRATION = 604800000L;
    public static final String SECRET_KEY =
            "eZTQb1Um2KE0dukTWfyHZSq2R3R1SFyqfRFfiReAPn1NHMKUBiTDKc5tajfn";

    public static final long FIRST_USER_ID = 1L;
    public static final long LAST_USER_ID = 2L;
    public static final long RANDOM_USER_ID = 1000L;

    public static final long FIRST_PROJECT_ID = 1L;
    public static final long ANOTHER_PROJECT_ID = 2L;
    public static final long RANDOM_PROJECT_ID = 1000L;
    public static final String NEW = "new";
    public static final int TEN = 10;
    public static final long FIRST_COMMENT_ID = 1L;
    public static final long RANDOM_COMMENT_ID = 1000L;
    public static final String COMMENT_TEXT = "I just thought that...";
    public static final String UPDATED_COMMENT_TEXT =
            "I just thought that you might be interested in...";

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private ProjectAuthorityUtil projectAuthorityUtil;

    @InjectMocks
    private CommentServiceImpl commentServiceImpl;

    private final Task task2 = Task.builder()
            .id(SECOND_TASK_ID)
            .name(TASK_NAME_2)
            .description(TASK_DESCRIPTION_2)
            .priority(Task.Priority.HIGH)
            .status(Task.Status.COMPLETED)
            .dueDate(TASK_DUE_DATE)
            .project(new Project())
            .assignee(new User())
            .isDeleted(true)
            .build();

    @Nested
    class AddComment {
        @Test
        void givenValidCommentDto_whenAddComment_thenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(user)
                    .managers(Set.of(user))
                    .employees(Set.of(user))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(user)
                    .isDeleted(false)
                    .build();

            CommentRequest validDto = new CommentRequest(
                    FIRST_TASK_ID,
                    COMMENT_TEXT);

            Comment expectedComment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(user)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            CommentResponse expectedCommentResponse = new CommentResponse(
                    FIRST_COMMENT_ID,
                    FIRST_TASK_ID,
                    FIRST_USER_ID,
                    COMMENT_TEXT,
                    expectedComment.getTimestamp());

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(task1.getProject().getId(), user.getId()))
                    .thenReturn(true);
            when(commentMapper.toAddComment(validDto, task1, user)).thenReturn(expectedComment);
            when(commentRepository.save(expectedComment)).thenReturn(expectedComment);
            when(commentMapper.toCommentDto(expectedComment)).thenReturn(expectedCommentResponse);

            //then
            assertEquals(expectedCommentResponse, commentServiceImpl.addComment(user, validDto));

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(task1.getProject().getId(), user.getId());
            verify(commentMapper, times(1)).toAddComment(validDto, task1, user);
            verify(commentRepository, times(1)).save(expectedComment);
            verify(commentMapper, times(1)).toCommentDto(expectedComment);
        }

        @Test
        void givenDeletedTask_whenAddComment_thenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            CommentRequest validDto = new CommentRequest(
                    SECOND_TASK_ID,
                    COMMENT_TEXT);

            //when
            when(taskRepository.findByIdNotDeleted(SECOND_TASK_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                        () -> commentServiceImpl.addComment(user, validDto));
            assertEquals("No active task with id " + validDto.taskId(),
                    entityNotFoundException.getMessage());

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(SECOND_TASK_ID);
        }

        @Test
        void givenTaskFromAlienProject_whenAddComment_thenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(user)
                    .managers(Set.of(user))
                    .employees(Set.of(user))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(user)
                    .isDeleted(false)
                    .build();

            CommentRequest validDto = new CommentRequest(
                    FIRST_TASK_ID,
                    COMMENT_TEXT);

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(task1.getProject().getId(), user.getId()))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException =
                    assertThrows(ForbiddenException.class,
                            () -> commentServiceImpl.addComment(user, validDto));
            assertEquals("You can't add comments to task " + validDto.taskId()
                    + " since you are not participant in project " + project.getId(),
                    forbiddenException.getMessage());

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(task1.getProject().getId(), user.getId());
        }
    }

    @Nested
    class UpdateComment {
        @Test
        void givenValidUpdateDto_whenUpdateComment_thenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(user)
                    .managers(Set.of(user))
                    .employees(Set.of(user))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(user)
                    .isDeleted(false)
                    .build();

            Comment expectedComment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(user)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            Comment updatedComment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(user)
                    .text(UPDATED_COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            CommentResponse expectedCommentResponse = new CommentResponse(
                    FIRST_COMMENT_ID,
                    FIRST_TASK_ID,
                    FIRST_USER_ID,
                    UPDATED_COMMENT_TEXT,
                    expectedComment.getTimestamp());

            //when
            when(commentRepository.findByIdAndUserId(FIRST_COMMENT_ID, FIRST_USER_ID))
                    .thenReturn(Optional.of(expectedComment));
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(task1.getProject().getId(), user.getId()))
                    .thenReturn(true);
            when(commentRepository.save(expectedComment)).thenReturn(updatedComment);
            when(commentMapper.toCommentDto(updatedComment)).thenReturn(expectedCommentResponse);

            //then
            assertEquals(expectedCommentResponse, commentServiceImpl.updateComment(
                    user.getId(),
                    new UpdateCommentRequest(UPDATED_COMMENT_TEXT),
                    FIRST_COMMENT_ID));
            assertThat(updatedComment)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedComment);

            //verify
            verify(commentRepository, times(1)).findByIdAndUserId(FIRST_COMMENT_ID, FIRST_USER_ID);
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(task1.getProject().getId(), user.getId());
            verify(commentRepository, times(1)).save(updatedComment);
            verify(commentMapper, times(1)).toCommentDto(updatedComment);
        }

        @Test
        void givenRandomCommentId_whenUpdateComment_thenSuccess() {
            //when
            when(commentRepository.findByIdAndUserId(RANDOM_COMMENT_ID, FIRST_USER_ID))
                    .thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> commentServiceImpl.updateComment(
                                FIRST_USER_ID,
                                new UpdateCommentRequest(COMMENT_TEXT),
                                RANDOM_COMMENT_ID));
            assertEquals("No comment with id " + RANDOM_COMMENT_ID
                    + " found for user " + FIRST_USER_ID, entityNotFoundException.getMessage());

            //verify
            verify(commentRepository, times(1))
                    .findByIdAndUserId(RANDOM_COMMENT_ID, FIRST_USER_ID);
        }

        @Test
        void givenForbiddenProject_whenUpdateComment_thenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(USERNAME_1)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_1)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User owner = User.builder()
                    .id(LAST_USER_ID)
                    .username(USERNAME_2)
                    .password(PASSWORD_1_DB)
                    .email(EMAIL_2)
                    .firstName(ANOTHER_FIRST_NAME)
                    .lastName(ANOTHER_LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(owner)
                    .managers(Set.of(owner))
                    .employees(Set.of(owner))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(user)
                    .isDeleted(false)
                    .build();

            Comment expectedComment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(user)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            //when
            when(commentRepository.findByIdAndUserId(FIRST_COMMENT_ID, FIRST_USER_ID))
                    .thenReturn(Optional.of(expectedComment));
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(task1.getProject().getId(), user.getId()))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> commentServiceImpl.updateComment(
                        user.getId(),
                        new UpdateCommentRequest(UPDATED_COMMENT_TEXT),
                        FIRST_COMMENT_ID));
            assertEquals("You can't update comments for task " + task1.getId()
                    + " since you are not participant in project " + project.getId(),
                    forbiddenException.getMessage());

            //verify
            verify(commentRepository, times(1)).findByIdAndUserId(FIRST_COMMENT_ID, FIRST_USER_ID);
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(task1.getProject().getId(), user.getId());
        }
    }
}
