package com.example.taskmanagementapp.services.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskRequest;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.response.TaskResponse;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.TaskMapper;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.LabelRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.services.email.TaskAssignmentEmailService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class TaskServiceImplTest {
    private static final String USERNAME_1 = "JohnDoe";
    private static final String PASSWORD_1 = "Best_Password1@3$";
    private static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String EMAIL_1 = "john_doe@mail.com";

    private static final String USERNAME_2 = "RichardRoe";
    private static final String PASSWORD_2 = "newPassword1@";
    private static final String EMAIL_2 = "richard_roe@mail.com";

    private static final String USERNAME_3 = "JaneDoe";
    private static final String PASSWORD_3 = "newPassword2@";
    private static final String EMAIL_3 = "jane_doe@mail.com";

    private static final String USERNAME_4 = "RickyRoe";
    private static final String EMAIL_4 = "ricky_roe@mail.com";

    private static final String USERNAME_5 = "TheBestJohnDoe";
    private static final String EMAIL_5 = "bestjohndoe@mail.com";

    private static final String USERNAME_6 = "TheNewJohnDoe";
    private static final String EMAIL_6 = "newjohndoe@mail.com";

    private static final String USERNAME_7 = "YetAnotherJohnDoe";
    private static final String EMAIL_7 = "yetanothertestjohndoe@mail.com";

    private static final String INVALID_USERNAME = "username@likemail.com";
    private static final String INVALID_EMAIL = "invalidmail.com";
    private static final String INVALID_PASSWORD = "password";
    private static final String ANOTHER_INVALID_PASSWORD = "new_password";
    private static final String EMPTY = "";

    private static final String ROLE_USER = "ROLE_USER";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final String ANOTHER_FIRST_NAME = "Richard";
    private static final String ANOTHER_LAST_NAME = "Roe";

    private static final String ACTION_TOKEN = "actionToken";
    private static final String NOT_EXISTING_ACTION_TOKEN = "blaBlaBlaActionToken";

    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    private static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);

    private static final String TASK_NAME_1 = "taskName";
    private static final String TASK_NAME_2 = "anotherTaskName";
    private static final String TASK_NAME_3 = "yetAnotherTaskName";
    private static final String TASK_DESCRIPTION_1 = "taskDescription";
    private static final String TASK_DESCRIPTION_2 = "anotherTaskDescription";
    private static final String TASK_DESCRIPTION_3 = "yetAnotherTaskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);

    private static final String LABEL_NAME_1 = "labelName";
    private static final String LABEL_NAME_2 = "anotherLabelName";

    private static final String FILE_ID_1 = "fileId1";
    private static final String FILE_NAME_1 = "fileName1";
    private static final String FILE_ID_2 = "fileId2";
    private static final String FILE_NAME_2 = "fileName2";
    private static final LocalDateTime UPLOADED_DATE = LocalDateTime.of(2025, 1, 6, 0, 0);

    private static final LocalDateTime TIME_STAMP = LocalDateTime.of(2025, 1, 6, 8, 30);
    private static final String COMMENT_TEXT_1 = "commentText1";
    private static final String COMMENT_TEXT_2 = "commentText2";

    private static final List<String> EXPECTED_ERRORS_ON_REGISTER = List.of(
            "firstName must not be blank.",
            "lastName must not be blank.",
            "password and repeatPassword don't match. Try again.",
            "password  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "repeatPassword  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "email : invalid email. Try again.",
            "username : invalid username. Can't be like email.");

    private static final long ULTRA_SHORT_EXPIRATION = 1L;
    private static final long ACTION_EXPIRATION = 60000L;
    private static final long ACCESS_EXPIRATION = 900000L;
    private static final long REFRESH_EXPIRATION = 604800000L;
    private static final String SECRET_KEY =
            "eZTQb1Um2KE0dukTWfyHZSq2R3R1SFyqfRFfiReAPn1NHMKUBiTDKc5tajfn";

    private static final long FIRST_USER_ID = 1L;
    private static final long LAST_USER_ID = 2L;
    private static final long RANDOM_USER_ID = 1000L;

    private static final long FIRST_PROJECT_ID = 1L;
    private static final long ANOTHER_PROJECT_ID = 2L;
    private static final long RANDOM_PROJECT_ID = 1000L;
    private static final String NEW = "new";
    private static final int TEN = 10;
    private static final long FIRST_TASK_ID = 1L;
    private static final long SECOND_TASK_ID = 2L;
    private static final long THIRD_TASK_ID = 3L;
    private static final int FIRST_PAGE = 0;
    private static final int PAGE_SIZE = 3;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private LabelRepository labelRepository;
    @Mock
    private TaskAssignmentEmailService taskAssignmentEmailService;
    @Mock
    private ProjectAuthorityUtil projectAuthorityUtil;
    
    @InjectMocks
    private TaskServiceImpl taskServiceImpl;

    @Nested
    class CreateTask {
        @Test
        void givenDeletedProject_whenCreateTask_ThenException() {
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

            Project deletedProject = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser))
                    .isDeleted(true)
                    .build();

            TaskRequest taskRequest = new TaskRequest(
                    TASK_NAME_1,
                    TASK_DESCRIPTION_1,
                    TASK_DUE_DATE,
                    deletedProject.getId(),
                    authenticatedUser.getId());

            //when
            when(projectRepository.findByIdNotDeleted(deletedProject.getId()))
                    .thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> taskServiceImpl.createTask(
                                    authenticatedUser,
                                    taskRequest,
                                    TaskPriorityDto.LOW));
            assertEquals("No active project with id " + deletedProject.getId(),
                    entityNotFoundException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(deletedProject.getId());
        }

        @Test
        void givenRandomUser_whenCreateTask_ThenException() {
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

            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser))
                    .build();

            TaskRequest taskRequest = new TaskRequest(
                    TASK_NAME_1,
                    TASK_DESCRIPTION_1,
                    TASK_DUE_DATE,
                    project.getId(),
                    RANDOM_USER_ID);

            //when
            when(projectRepository.findByIdNotDeleted(project.getId()))
                    .thenReturn(Optional.of(project));
            when(userRepository.findById(RANDOM_USER_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> taskServiceImpl.createTask(
                                    authenticatedUser,
                                    taskRequest,
                                    TaskPriorityDto.LOW));
            assertEquals("No user with id " + RANDOM_USER_ID,
                    entityNotFoundException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(project.getId());
            verify(userRepository, times(1)).findById(RANDOM_USER_ID);
        }

        @Test
        void givenForbiddenProject_whenCreateTask_ThenException() {
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
                    .employees(Set.of(owner, authenticatedUser))
                    .build();

            TaskRequest taskRequest = new TaskRequest(
                    TASK_NAME_1,
                    TASK_DESCRIPTION_1,
                    TASK_DUE_DATE,
                    project.getId(),
                    owner.getId());

            //when
            when(projectRepository.findByIdNotDeleted(project.getId()))
                    .thenReturn(Optional.of(project));
            when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    project.getId(), authenticatedUser.getId()))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException =
                    assertThrows(ForbiddenException.class,
                            () -> taskServiceImpl.createTask(
                                    authenticatedUser,
                                    taskRequest,
                                    TaskPriorityDto.LOW));
            assertEquals("You have no permission to modify project " + project.getId(),
                    forbiddenException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(project.getId());
            verify(userRepository, times(1)).findById(owner.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(project.getId(), authenticatedUser.getId());
        }

        @Test
        void givenAssigneeNotFromProject_whenCreateTask_ThenException() {
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

            User assigneeOutsider = User.builder()
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
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser))
                    .build();

            TaskRequest taskRequest = new TaskRequest(
                    TASK_NAME_1,
                    TASK_DESCRIPTION_1,
                    TASK_DUE_DATE,
                    project.getId(),
                    assigneeOutsider.getId());

            //when
            when(projectRepository.findByIdNotDeleted(project.getId()))
                    .thenReturn(Optional.of(project));
            when(userRepository.findById(assigneeOutsider.getId()))
                    .thenReturn(Optional.of(assigneeOutsider));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    project.getId(), authenticatedUser.getId()))
                    .thenReturn(true);
            when(projectAuthorityUtil.hasAnyAuthority(project.getId(), assigneeOutsider.getId()))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException =
                    assertThrows(ForbiddenException.class,
                            () -> taskServiceImpl.createTask(
                                    authenticatedUser,
                                    taskRequest,
                                    TaskPriorityDto.LOW));
            assertEquals("User " + assigneeOutsider.getId() + " is not assigned to project "
                            + project.getId(),
                    forbiddenException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(project.getId());
            verify(userRepository, times(1)).findById(assigneeOutsider.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(project.getId(), authenticatedUser.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(project.getId(), assigneeOutsider.getId());
        }

        @Test
        void givenValidRequest_whenCreateTask_ThenSuccess() throws ForbiddenException {
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

            User assignee = User.builder()
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
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser, assignee))
                    .build();

            TaskRequest taskRequest = new TaskRequest(
                    TASK_NAME_1,
                    TASK_DESCRIPTION_1,
                    TASK_DUE_DATE,
                    project.getId(),
                    assignee.getId());

            TaskPriorityDto taskPriorityDto = TaskPriorityDto.LOW;

            Task taskFromMapper = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(taskRequest.name())
                    .description(taskRequest.description())
                    .dueDate(taskRequest.dueDate())
                    .project(project)
                    .assignee(assignee)
                    .isDeleted(false)
                    .build();

            Task initializedTask = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(taskRequest.name())
                    .description(taskRequest.description())
                    .dueDate(taskRequest.dueDate())
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(assignee)
                    .isDeleted(false)
                    .build();

            TaskResponse taskResponse = TaskResponse.builder()
                    .id(FIRST_TASK_ID)
                    .name(taskRequest.name())
                    .description(taskRequest.description())
                    .dueDate(taskRequest.dueDate())
                    .projectId(project.getId())
                    .assigneeId(assignee.getId())
                    .status(TaskStatusDto.NOT_STARTED)
                    .priority(taskPriorityDto)
                    .build();

            //when
            when(projectRepository.findByIdNotDeleted(project.getId()))
                    .thenReturn(Optional.of(project));
            when(userRepository.findById(assignee.getId()))
                    .thenReturn(Optional.of(assignee));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    project.getId(), authenticatedUser.getId()))
                    .thenReturn(true);
            when(projectAuthorityUtil.hasAnyAuthority(project.getId(), assignee.getId()))
                    .thenReturn(true);
            when(taskMapper.toCreateTask(taskRequest)).thenReturn(taskFromMapper);
            when(taskRepository.save(taskFromMapper)).thenReturn(initializedTask);
            when(taskMapper.toTaskDto(initializedTask)).thenReturn(taskResponse);
            //then
            assertEquals(taskResponse, taskServiceImpl.createTask(
                    authenticatedUser, taskRequest, taskPriorityDto));
            assertThat(taskFromMapper)
                    .isNotNull()
                    .isEqualTo(initializedTask);

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(project.getId());
            verify(userRepository, times(1)).findById(assignee.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(project.getId(), authenticatedUser.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasAnyAuthority(project.getId(), assignee.getId());
            verify(taskMapper, times(1)).toCreateTask(taskRequest);
            verify(taskRepository, times(1)).save(taskFromMapper);
            verify(taskMapper, times(1)).toTaskDto(initializedTask);
        }
    }

    @Nested
    class GetTasksForProject {
        @Test
        void givenDeletedProject_whenGetTasksForProject_thenException() throws ForbiddenException {
            //when
            when(projectRepository.existsByIdNotDeleted(FIRST_PROJECT_ID)).thenReturn(false);

            //then
            assertThrows(EntityNotFoundException.class,
                    () -> taskServiceImpl.getTasksForProject(
                            FIRST_USER_ID, FIRST_PROJECT_ID, Pageable.unpaged()));

            //verify
            verify(projectRepository, times(1)).existsByIdNotDeleted(FIRST_PROJECT_ID);
        }

        @Test
        void givenGoodRequest_whenGetTasksForProject_thenSuccess() throws ForbiddenException {
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

            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser))
                    .build();

            Task firstTask = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME_1)
                    .description(TASK_DESCRIPTION_1)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task secondTask = Task.builder()
                    .id(SECOND_TASK_ID)
                    .name(TASK_NAME_2)
                    .description(TASK_DESCRIPTION_2)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.MEDIUM)
                    .status(Task.Status.COMPLETED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task thirdTask = Task.builder()
                    .id(THIRD_TASK_ID)
                    .name(TASK_NAME_3)
                    .description(TASK_DESCRIPTION_3)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.HIGH)
                    .status(Task.Status.IN_PROGRESS)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            TaskResponse firstTaskResponse = TaskResponse.builder()
                    .id(firstTask.getId())
                    .name(firstTask.getName())
                    .description(firstTask.getDescription())
                    .dueDate(firstTask.getDueDate())
                    .projectId(firstTask.getProject().getId())
                    .assigneeId(firstTask.getAssignee().getId())
                    .status(TaskStatusDto.valueOf(firstTask.getStatus().name()))
                    .priority(TaskPriorityDto.valueOf(firstTask.getPriority().name()))
                    .build();

            TaskResponse secondTaskResponse = TaskResponse.builder()
                    .id(secondTask.getId())
                    .name(secondTask.getName())
                    .description(secondTask.getDescription())
                    .dueDate(secondTask.getDueDate())
                    .projectId(secondTask.getProject().getId())
                    .assigneeId(secondTask.getAssignee().getId())
                    .status(TaskStatusDto.valueOf(secondTask.getStatus().name()))
                    .priority(TaskPriorityDto.valueOf(secondTask.getPriority().name()))
                    .build();

            TaskResponse thirdTaskResponse = TaskResponse.builder()
                    .id(thirdTask.getId())
                    .name(thirdTask.getName())
                    .description(thirdTask.getDescription())
                    .dueDate(thirdTask.getDueDate())
                    .projectId(thirdTask.getProject().getId())
                    .assigneeId(thirdTask.getAssignee().getId())
                    .status(TaskStatusDto.valueOf(thirdTask.getStatus().name()))
                    .priority(TaskPriorityDto.valueOf(thirdTask.getPriority().name()))
                    .build();

            Page<Task> tasks = new PageImpl<>(List.of(firstTask, secondTask, thirdTask));
            List<TaskResponse> taskResponses =
                    List.of(firstTaskResponse, secondTaskResponse, thirdTaskResponse);
            Pageable pageable = PageRequest.of(FIRST_PAGE, PAGE_SIZE);

            //when
            when(projectRepository.existsByIdNotDeleted(FIRST_PROJECT_ID)).thenReturn(true);
            when(projectAuthorityUtil.hasAnyAuthority(FIRST_PROJECT_ID, FIRST_TASK_ID))
                    .thenReturn(true);
            when(taskRepository.findAllByProjectIdNonDeleted(project.getId(), pageable))
                    .thenReturn(tasks);
            when(taskMapper.toTaskDtoList(tasks.getContent())).thenReturn(taskResponses);

            //then
            assertEquals(taskResponses,
                    taskServiceImpl.getTasksForProject(
                            authenticatedUser.getId(), project.getId(), pageable));
        }

        @Test
        void givenForbiddenProject_whenGetTasksForProject_thenSuccess() throws ForbiddenException {
            //when
            when(projectRepository.existsByIdNotDeleted(FIRST_PROJECT_ID)).thenReturn(true);
            when(projectAuthorityUtil.hasAnyAuthority(FIRST_PROJECT_ID, FIRST_TASK_ID))
                    .thenReturn(false);
            //then
            assertThrows(ForbiddenException.class,
                    () -> taskServiceImpl.getTasksForProject(
                            FIRST_USER_ID, FIRST_PROJECT_ID, Pageable.unpaged()));

            //verify
            verify(projectRepository).existsByIdNotDeleted(FIRST_PROJECT_ID);
            verify(projectAuthorityUtil).hasAnyAuthority(FIRST_PROJECT_ID, FIRST_TASK_ID);
        }
    }

    @Nested
    class GetTaskById {

    }
}
