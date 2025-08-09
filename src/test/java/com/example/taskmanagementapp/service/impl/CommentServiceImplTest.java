package com.example.taskmanagementapp.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dto.comment.request.CommentRequest;
import com.example.taskmanagementapp.dto.comment.request.UpdateCommentRequest;
import com.example.taskmanagementapp.dto.comment.response.CommentResponse;
import com.example.taskmanagementapp.entity.Comment;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.mapper.CommentMapper;
import com.example.taskmanagementapp.repository.CommentRepository;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.service.utils.ProjectAuthorityUtil;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
    private static final String USERNAME_1 = "JohnDoe";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";

    private static final String USERNAME_2 = "RichardRoe";
    private static final String EMAIL_2 = "richard_roe@mail.com";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final String ANOTHER_FIRST_NAME = "Richard";
    private static final String ANOTHER_LAST_NAME = "Roe";

    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);

    private static final Long FIRST_TASK_ID = 1L;
    private static final Long SECOND_TASK_ID = 2L;
    private static final String TASK_NAME_1 = "taskName";
    private static final String TASK_DESCRIPTION_1 = "taskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);

    private static final long FIRST_USER_ID = 1L;
    private static final long LAST_USER_ID = 2L;
    private static final long RANDOM_USER_ID = 1000L;

    private static final long FIRST_PROJECT_ID = 1L;
    private static final long FIRST_COMMENT_ID = 1L;
    private static final long SECOND_COMMENT_ID = 2L;
    private static final long RANDOM_COMMENT_ID = 1000L;
    private static final String COMMENT_TEXT = "I just thought that...";
    private static final String UPDATED_COMMENT_TEXT =
            "I just thought that you might be interested in...";
    private static final int FIRST_PAGE = 0;
    private static final int PAGE_SIZE = 3;

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

            LocalDateTime commentTimeStamp = LocalDateTime.now();
            Comment expectedComment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(user)
                    .text(COMMENT_TEXT)
                    .timestamp(commentTimeStamp)
                    .build();

            Comment updatedComment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(user)
                    .text(UPDATED_COMMENT_TEXT)
                    .timestamp(commentTimeStamp)
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

    @Nested
    class GetAllComments {
        @Test
        void givenAllowedProject_whenGetAllComments_thenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

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
                    .assignee(owner)
                    .isDeleted(false)
                    .build();

            Comment comment1 = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(owner)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            Comment comment2 = Comment.builder()
                    .id(SECOND_COMMENT_ID)
                    .task(task1)
                    .user(owner)
                    .text(UPDATED_COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            Page<Comment> commentPage = new PageImpl<>(List.of(comment1, comment2));

            List<CommentResponse> commentResponses = List.of(
                    new CommentResponse(
                            comment1.getId(),
                            comment1.getTask().getId(),
                            comment1.getUser().getId(),
                            comment1.getText(),
                            comment1.getTimestamp()),
                    new CommentResponse(
                            comment2.getId(),
                            comment2.getTask().getId(),
                            comment2.getUser().getId(),
                            comment2.getText(),
                            comment2.getTimestamp()));

            Pageable pageable = PageRequest.of(FIRST_PAGE, PAGE_SIZE);
            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(project.getId(), owner.getId()))
                    .thenReturn(true);
            when(commentRepository.findAllByTaskId(FIRST_TASK_ID, pageable))
                    .thenReturn(commentPage);
            when(commentMapper.toCommentDtoList(commentPage.getContent()))
                    .thenReturn(commentResponses);

            //then
            assertEquals(commentResponses, commentServiceImpl
                    .getAllComments(owner.getId(), task1.getId(), pageable));

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1)).hasAnyAuthority(task1.getId(), owner.getId());
            verify(commentRepository, times(1)).findAllByTaskId(FIRST_TASK_ID, pageable);
            verify(commentMapper, times(1)).toCommentDtoList(commentPage.getContent());
        }

        @Test
        void givenForbiddenProject_whenGetAllComments_thenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

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
                    .assignee(owner)
                    .isDeleted(false)
                    .build();
            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(project.getId(), RANDOM_USER_ID))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> commentServiceImpl.getAllComments(
                            RANDOM_USER_ID, FIRST_TASK_ID, Pageable.unpaged()));
            assertEquals("You can't get comments for task " + task1.getId()
                            + " since you are not participant in project " + project.getId(),
                    forbiddenException.getMessage());

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1)).hasAnyAuthority(project.getId(), RANDOM_USER_ID);
        }
    }

    @Nested
    class DeleteComment {
        @Test
        void givenSomeoneElseComment_whenDeleteComment_ThenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
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

            User commentOwner = User.builder()
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
                    .owner(commentOwner)
                    .managers(Set.of(commentOwner, authenticatedUser))
                    .employees(Set.of(commentOwner, authenticatedUser))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(commentOwner)
                    .isDeleted(false)
                    .build();

            Comment comment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(commentOwner)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            //when
            when(commentRepository.findByIdAndUserId(comment.getId(), authenticatedUser.getId()))
                    .thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                        () -> commentServiceImpl.deleteComment(
                                authenticatedUser.getId(), comment.getId()));
            assertEquals("No comment with id " + comment.getId()
                    + " found for user " + authenticatedUser.getId(),
                    entityNotFoundException.getMessage());

            //verify
            verify(commentRepository, times(1))
                    .findByIdAndUserId(comment.getId(), authenticatedUser.getId());
        }

        @Test
        void givenDisabledTask_whenDeleteComment_ThenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
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

            User commentOwner = User.builder()
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
                    .owner(commentOwner)
                    .managers(Set.of(commentOwner, authenticatedUser))
                    .employees(Set.of(commentOwner, authenticatedUser))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(commentOwner)
                    .isDeleted(false)
                    .build();

            Comment comment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(authenticatedUser)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            //when
            when(commentRepository.findByIdAndUserId(comment.getId(), authenticatedUser.getId()))
                    .thenReturn(Optional.of(comment));
            when(taskRepository.findByIdNotDeleted(task1.getId())).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> commentServiceImpl.deleteComment(
                                    authenticatedUser.getId(), comment.getId()));
            assertEquals("No active task with id " + task1.getId(),
                    entityNotFoundException.getMessage());

            //verify
            verify(commentRepository, times(1))
                    .findByIdAndUserId(comment.getId(), authenticatedUser.getId());
            verify(taskRepository, times(1)).findByIdNotDeleted(task1.getId());
        }

        @Test
        void givenForbiddenProject_whenDeleteComment_ThenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
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

            User commentOwner = User.builder()
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
                    .owner(commentOwner)
                    .managers(Set.of(commentOwner))
                    .employees(Set.of(commentOwner))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(commentOwner)
                    .isDeleted(false)
                    .build();

            Comment comment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(authenticatedUser)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            //when
            when(commentRepository.findByIdAndUserId(comment.getId(), authenticatedUser.getId()))
                    .thenReturn(Optional.of(comment));
            when(taskRepository.findByIdNotDeleted(task1.getId())).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(project.getId(), authenticatedUser.getId()))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException =
                    assertThrows(ForbiddenException.class,
                            () -> commentServiceImpl.deleteComment(
                                    authenticatedUser.getId(), comment.getId()));
            assertEquals("You can't delete comments from task " + task1.getId()
                            + " since you are not participant in project " + project.getId(),
                    forbiddenException.getMessage());

            //verify
            verify(commentRepository, times(1))
                    .findByIdAndUserId(comment.getId(), authenticatedUser.getId());
            verify(taskRepository, times(1)).findByIdNotDeleted(task1.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(project.getId(), authenticatedUser.getId());
        }

        @Test
        void givenGoodRequest_whenDeleteComment_ThenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
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

            User projectOwner = User.builder()
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
                    .owner(projectOwner)
                    .managers(Set.of(projectOwner, authenticatedUser))
                    .employees(Set.of(projectOwner, authenticatedUser))
                    .build();

            Task task1 = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .dueDate(TASK_DUE_DATE)
                    .project(project)
                    .assignee(projectOwner)
                    .isDeleted(false)
                    .build();

            Comment comment = Comment.builder()
                    .id(FIRST_COMMENT_ID)
                    .task(task1)
                    .user(authenticatedUser)
                    .text(COMMENT_TEXT)
                    .timestamp(LocalDateTime.now())
                    .build();

            //when
            when(commentRepository.findByIdAndUserId(comment.getId(), authenticatedUser.getId()))
                    .thenReturn(Optional.of(comment));
            when(taskRepository.findByIdNotDeleted(task1.getId())).thenReturn(Optional.of(task1));
            when(projectAuthorityUtil.hasAnyAuthority(project.getId(), authenticatedUser.getId()))
                    .thenReturn(true);
            //then
            commentServiceImpl.deleteComment(authenticatedUser.getId(), comment.getId());

            //verify
            verify(commentRepository, times(1))
                    .findByIdAndUserId(comment.getId(), authenticatedUser.getId());
            verify(taskRepository, times(1)).findByIdNotDeleted(task1.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(project.getId(), authenticatedUser.getId());
            verify(commentRepository, times(1)).deleteById(comment.getId());
        }
    }
}
