package com.example.taskmanagementapp.service;

import static com.example.taskmanagementapp.constant.security.SecurityConstants.NO_ACCESS_PERMISSION_FOR_TASK;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.NO_PERMISSION_FOR_TASK_DELETION;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dto.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dto.task.request.TaskRequest;
import com.example.taskmanagementapp.dto.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dto.task.request.UpdateTaskRequest;
import com.example.taskmanagementapp.dto.task.response.TaskResponse;
import com.example.taskmanagementapp.entity.Project;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.Task;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.mapper.TaskMapper;
import com.example.taskmanagementapp.repository.CommentRepository;
import com.example.taskmanagementapp.repository.LabelRepository;
import com.example.taskmanagementapp.repository.ProjectRepository;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.email.TaskAssignmentEmailService;
import com.example.taskmanagementapp.service.impl.TaskServiceImpl;
import com.example.taskmanagementapp.service.utils.ProjectAuthorityUtil;
import java.time.LocalDate;
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
    private static final String TEST_USERNAME = "JohnDoe";
    private static final String TEST_PASSWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String TEST_EMAIL = "john_doe@mail.com";

    private static final String ANOTHER_TEST_USERNAME = "RichardRoe";
    private static final String ANOTHER_TEST_EMAIL = "richard_roe@mail.com";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final String ANOTHER_FIRST_NAME = "Richard";
    private static final String ANOTHER_LAST_NAME = "Roe";

    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    private static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);

    private static final String TASK_NAME = "taskName";
    private static final String ANOTHER_TASK_NAME = "anotherTaskName";
    private static final String YET_ANOTHER_TASK_NAME = "yetAnotherTaskName";
    private static final String TASK_DESCRIPTION = "taskDescription";
    private static final String ANOTHER_TASK_DESCRIPTION = "anotherTaskDescription";
    private static final String YET_ANOTHER_TASK_DESCRIPTION = "yetAnotherTaskDescription";
    private static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);

    private static final long FIRST_USER_ID = 1L;
    private static final long ANOTHER_USER_ID = 2L;
    private static final long RANDOM_USER_ID = 1000L;

    private static final long FIRST_PROJECT_ID = 1L;
    private static final long ANOTHER_PROJECT_ID = 2L;
    private static final long RANDOM_PROJECT_ID = 1000L;
    private static final int TEN = 10;
    private static final long FIRST_TASK_ID = 1L;
    private static final long SECOND_TASK_ID = 2L;
    private static final long THIRD_TASK_ID = 3L;
    private static final long RANDOM_TASK_ID = 1000L;
    private static final int FIRST_PAGE = 0;
    private static final int PAGE_SIZE = 3;
    private static final long FIRST_LABEL_ID = 1;
    private static final long RANDOM_LABEL_ID = 1000;
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
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
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
                    TASK_NAME,
                    TASK_DESCRIPTION,
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
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
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
                    TASK_NAME,
                    TASK_DESCRIPTION,
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
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User owner = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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
                    TASK_NAME,
                    TASK_DESCRIPTION,
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
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User assigneeOutsider = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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
                    TASK_NAME,
                    TASK_DESCRIPTION,
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
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User assignee = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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
                    TASK_NAME,
                    TASK_DESCRIPTION,
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
        void givenDeletedProject_whenGetTasksForProject_thenException() {
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
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
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
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task secondTask = Task.builder()
                    .id(SECOND_TASK_ID)
                    .name(ANOTHER_TASK_NAME)
                    .description(ANOTHER_TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.MEDIUM)
                    .status(Task.Status.COMPLETED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task thirdTask = Task.builder()
                    .id(THIRD_TASK_ID)
                    .name(YET_ANOTHER_TASK_NAME)
                    .description(YET_ANOTHER_TASK_DESCRIPTION)
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
        void givenForbiddenProject_whenGetTasksForProject_thenSuccess() {
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
        @Test
        void givenTaskId_whenGetTaskById_thenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User assignee = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(assignee)
                    .isDeleted(false)
                    .build();

            TaskResponse taskResponse = TaskResponse.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .projectId(project.getId())
                    .assigneeId(assignee.getId())
                    .status(TaskStatusDto.NOT_STARTED)
                    .priority(TaskPriorityDto.LOW)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasAnyAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(taskMapper.toTaskDto(taskFromRepo)).thenReturn(taskResponse);

            //then
            assertEquals(taskResponse,
                    taskServiceImpl.getTaskById(authenticatedUser.getId(), FIRST_TASK_ID));

            //verify
            verify(taskRepository).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil).hasAnyAuthority(FIRST_PROJECT_ID, FIRST_USER_ID);
            verify(taskMapper).toTaskDto(taskFromRepo);
        }

        @Test
        void givenRandomTaskId_whenGetTaskById_thenException() {
            //when
            when(taskRepository.findByIdNotDeleted(RANDOM_TASK_ID))
                    .thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> taskServiceImpl.getTaskById(FIRST_USER_ID, RANDOM_TASK_ID));
            assertEquals("No active task with id "
                    + RANDOM_TASK_ID, entityNotFoundException.getMessage());

            //verify
            verify(taskRepository).findByIdNotDeleted(RANDOM_TASK_ID);
        }

        @Test
        void givenTaskIdFromForbiddenProject_whenGetTaskById_thenSuccess() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User owner = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(owner)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasAnyAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> taskServiceImpl.getTaskById(authenticatedUser.getId(), FIRST_TASK_ID));
            assertEquals(NO_ACCESS_PERMISSION_FOR_TASK, forbiddenException.getMessage());

            //verify
            verify(taskRepository).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil).hasAnyAuthority(FIRST_PROJECT_ID, FIRST_USER_ID);
        }
    }

    @Nested
    class UpdateTask {
        @Test
        void givenValidUpdateRequest_whenUpdateTask_thenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User assignee = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Project anotherProject = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.IN_PROGRESS)
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser, assignee))
                    .build();

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task taskFromRepoAfterUpdate = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(ANOTHER_TASK_NAME)
                    .description(ANOTHER_TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.MEDIUM)
                    .status(Task.Status.IN_PROGRESS)
                    .project(project)
                    .assignee(assignee)
                    .isDeleted(false)
                    .build();

            TaskResponse taskResponse = TaskResponse.builder()
                    .id(FIRST_TASK_ID)
                    .name(ANOTHER_TASK_NAME)
                    .description(ANOTHER_TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .projectId(anotherProject.getId())
                    .assigneeId(assignee.getId())
                    .status(TaskStatusDto.IN_PROGRESS)
                    .priority(TaskPriorityDto.MEDIUM)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(projectRepository.findByIdNotDeleted(ANOTHER_PROJECT_ID))
                    .thenReturn(Optional.of(anotherProject));
            when(projectAuthorityUtil.hasManagerialAuthority(ANOTHER_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(userRepository.findById(ANOTHER_USER_ID)).thenReturn(Optional.of(assignee));
            when(projectRepository.isUserEmployee(ANOTHER_PROJECT_ID, ANOTHER_USER_ID))
                    .thenReturn(true);
            when(taskRepository.save(taskFromRepo)).thenReturn(taskFromRepoAfterUpdate);
            when(taskMapper.toTaskDto(taskFromRepoAfterUpdate)).thenReturn(taskResponse);

            //then
            assertEquals(taskResponse, taskServiceImpl.updateTask(
                    authenticatedUser,
                    new UpdateTaskRequest(
                            ANOTHER_TASK_NAME,
                            ANOTHER_TASK_DESCRIPTION,
                            TASK_DUE_DATE.plusDays(TEN),
                            anotherProject.getId(),
                            assignee.getId()),
                    FIRST_TASK_ID,
                    TaskStatusDto.IN_PROGRESS,
                    TaskPriorityDto.MEDIUM));
        }

        @Test
        void givenNotExistingTask_whenUpdateTask_thenNotFoundException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException = assertThrows(
                    EntityNotFoundException.class, () -> taskServiceImpl.updateTask(
                            authenticatedUser,
                            new UpdateTaskRequest(
                                    ANOTHER_TASK_NAME,
                                    ANOTHER_TASK_DESCRIPTION,
                                    TASK_DUE_DATE,
                                    FIRST_PROJECT_ID,
                                    FIRST_USER_ID),
                            RANDOM_TASK_ID,
                            TaskStatusDto.NOT_STARTED,
                            TaskPriorityDto.MEDIUM));
            assertEquals("No active task with id " + RANDOM_TASK_ID,
                    entityNotFoundException.getMessage());

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(RANDOM_TASK_ID);
        }

        @Test
        void givenForbiddenProject_whenUpdateTask_thenForbiddenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            Project project = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.IN_PROGRESS)
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser))
                    .build();

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();
            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    project.getId(), authenticatedUser.getId())).thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(
                    ForbiddenException.class, () -> taskServiceImpl.updateTask(
                            authenticatedUser,
                            new UpdateTaskRequest(
                                    ANOTHER_TASK_NAME,
                                    ANOTHER_TASK_DESCRIPTION,
                                    TASK_DUE_DATE,
                                    FIRST_PROJECT_ID,
                                    FIRST_USER_ID),
                            FIRST_TASK_ID,
                            TaskStatusDto.NOT_STARTED,
                            TaskPriorityDto.MEDIUM));
            assertEquals(NO_ACCESS_PERMISSION_FOR_TASK,
                    forbiddenException.getMessage());

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(project.getId(), authenticatedUser.getId());
        }

        @Test
        void givenNotExistingProjectInUpdate_whenUpdateTask_thenNotFoundException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User assignee = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(projectRepository.findByIdNotDeleted(RANDOM_PROJECT_ID))
                    .thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                        () -> taskServiceImpl.updateTask(authenticatedUser,
                            new UpdateTaskRequest(
                                    ANOTHER_TASK_NAME,
                                    ANOTHER_TASK_DESCRIPTION,
                                    null,
                                    RANDOM_PROJECT_ID,
                                    assignee.getId()),
                            FIRST_TASK_ID,
                            TaskStatusDto.IN_PROGRESS,
                            TaskPriorityDto.MEDIUM));
            assertEquals("No project with id " + RANDOM_PROJECT_ID,
                    entityNotFoundException.getMessage());

            verify(taskRepository, times(1)).findByIdNotDeleted(FIRST_TASK_ID);
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID);
            verify(projectRepository, times(1)).findByIdNotDeleted(RANDOM_PROJECT_ID);
        }

        @Test
        void givenForbiddenProjectInUpdate_whenUpdateTask_thenForbiddenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User owner = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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
                    .employees(Set.of(authenticatedUser, owner))
                    .build();

            Project anotherProject = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.IN_PROGRESS)
                    .owner(owner)
                    .managers(Set.of(owner))
                    .employees(Set.of(owner))
                    .build();

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(projectRepository.findByIdNotDeleted(ANOTHER_PROJECT_ID))
                    .thenReturn(Optional.of(anotherProject));

            //then
            ForbiddenException forbiddenException =
                    assertThrows(ForbiddenException.class,
                            () -> taskServiceImpl.updateTask(authenticatedUser,
                                    new UpdateTaskRequest(
                                            ANOTHER_TASK_NAME,
                                            ANOTHER_TASK_DESCRIPTION,
                                            null,
                                            anotherProject.getId(),
                                            owner.getId()),
                                    FIRST_TASK_ID,
                                    TaskStatusDto.IN_PROGRESS,
                                    TaskPriorityDto.MEDIUM));
            assertEquals("You have no permission to modify project " + anotherProject.getId(),
                    forbiddenException.getMessage());
        }

        @Test
        void givenNotExistingAssigneeInUpdate_whenUpdateTask_thenNotFoundException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
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

            Project anotherProject = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.IN_PROGRESS)
                    .owner(authenticatedUser)
                    .managers(Set.of(authenticatedUser))
                    .employees(Set.of(authenticatedUser))
                    .build();

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(projectRepository.findByIdNotDeleted(ANOTHER_PROJECT_ID))
                    .thenReturn(Optional.of(anotherProject));
            when(projectAuthorityUtil.hasManagerialAuthority(ANOTHER_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(userRepository.findById(RANDOM_USER_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> taskServiceImpl.updateTask(authenticatedUser,
                                    new UpdateTaskRequest(
                                            ANOTHER_TASK_NAME,
                                            ANOTHER_TASK_DESCRIPTION,
                                            null,
                                            anotherProject.getId(),
                                            RANDOM_USER_ID),
                                    FIRST_TASK_ID,
                                    TaskStatusDto.IN_PROGRESS,
                                    TaskPriorityDto.MEDIUM));
            assertEquals("No user with id " + RANDOM_USER_ID,
                    entityNotFoundException.getMessage());
        }

        @Test
        void givenAssigneeInUpdateNotInProject_whenUpdateTask_thenForbiddenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            User assignee = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(projectAuthorityUtil.hasManagerialAuthority(ANOTHER_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(true);
            when(userRepository.findById(ANOTHER_USER_ID)).thenReturn(Optional.of(assignee));

            //then
            ForbiddenException forbiddenException =
                    assertThrows(ForbiddenException.class,
                            () -> taskServiceImpl.updateTask(authenticatedUser,
                                    new UpdateTaskRequest(
                                            ANOTHER_TASK_NAME,
                                            ANOTHER_TASK_DESCRIPTION,
                                            null,
                                            null,
                                            ANOTHER_USER_ID),
                                    FIRST_TASK_ID,
                                    TaskStatusDto.IN_PROGRESS,
                                    TaskPriorityDto.MEDIUM));
            assertEquals("You can't assign employee "
                            + ANOTHER_USER_ID + " to task " + FIRST_TASK_ID
                            + " since they are not in project " + FIRST_PROJECT_ID,
                    forbiddenException.getMessage());
        }
    }

    @Nested
    class DeleteTask {
        @Test
        void givenNotExistingTask_whenDeleteTask_thenNotFoundException() {
            //when
            when(taskRepository.findByIdNotDeleted(RANDOM_TASK_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                        () -> taskServiceImpl.deleteTask(FIRST_USER_ID, RANDOM_TASK_ID));
            assertEquals("No active task with id "
                    + RANDOM_TASK_ID, entityNotFoundException.getMessage());

            //verify
            verify(taskRepository, times(1)).findByIdNotDeleted(RANDOM_TASK_ID);
        }

        @Test
        void givenForbiddenProject_whenDeleteTask_thenForbiddenException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User owner = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(owner)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(FIRST_PROJECT_ID, FIRST_USER_ID))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> taskServiceImpl.deleteTask(FIRST_USER_ID, FIRST_TASK_ID));
            assertEquals(NO_PERMISSION_FOR_TASK_DELETION, forbiddenException.getMessage());
        }

        @Test
        void givenValidRequest_whenDeleteTask_thenSuccess() throws ForbiddenException {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
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

            Task taskFromRepo = Task.builder()
                    .id(FIRST_TASK_ID)
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            //when
            when(taskRepository.findByIdNotDeleted(FIRST_TASK_ID))
                    .thenReturn(Optional.of(taskFromRepo));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    project.getId(), authenticatedUser.getId()))
                        .thenReturn(true);

            //then
            taskServiceImpl.deleteTask(authenticatedUser.getId(), taskFromRepo.getId());

            //verify
            verify(taskRepository, times(1)).deleteById(taskFromRepo.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(project.getId(), authenticatedUser.getId());
            verify(commentRepository, times(1)).deleteAllByTaskId(taskFromRepo.getId());
            verify(taskRepository, times(1)).deleteById(taskFromRepo.getId());
        }
    }

    @Nested
    class GetTasksWithLabel {
        @Test
        void givenValidRequest_whenGetTasksWithLabel_thenSuccess() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();

            User authenticatedUser = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
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
                    .name(TASK_NAME)
                    .description(TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.LOW)
                    .status(Task.Status.NOT_STARTED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task secondTask = Task.builder()
                    .id(SECOND_TASK_ID)
                    .name(ANOTHER_TASK_NAME)
                    .description(ANOTHER_TASK_DESCRIPTION)
                    .dueDate(TASK_DUE_DATE)
                    .priority(Task.Priority.MEDIUM)
                    .status(Task.Status.COMPLETED)
                    .project(project)
                    .assignee(authenticatedUser)
                    .isDeleted(false)
                    .build();

            Task thirdTask = Task.builder()
                    .id(THIRD_TASK_ID)
                    .name(YET_ANOTHER_TASK_NAME)
                    .description(YET_ANOTHER_TASK_DESCRIPTION)
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
            when(labelRepository.existsByIdAndUserId(FIRST_LABEL_ID, authenticatedUser.getId()))
                    .thenReturn(true);
            when(taskRepository.findAllByLabelIdNonDeleted(FIRST_LABEL_ID, pageable))
                    .thenReturn(tasks);
            when(taskMapper.toTaskDtoList(tasks.getContent())).thenReturn(taskResponses);

            //then
            assertEquals(taskResponses, taskServiceImpl.getTasksWithLabel(
                    authenticatedUser.getId(), FIRST_LABEL_ID, pageable));

            //verify
            verify(labelRepository).existsByIdAndUserId(FIRST_LABEL_ID, authenticatedUser.getId());
            verify(taskRepository).findAllByLabelIdNonDeleted(FIRST_LABEL_ID, pageable);
            verify(taskMapper).toTaskDtoList(tasks.getContent());
        }

        @Test
        void givenNotExistingLabel_whenGetTasksWithLabel_thenNotFoundException() {
            //when
            when(labelRepository.existsByIdAndUserId(RANDOM_LABEL_ID, FIRST_USER_ID))
                    .thenReturn(false);

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                        () -> taskServiceImpl.getTasksWithLabel(
                                FIRST_USER_ID, RANDOM_LABEL_ID, Pageable.unpaged()));
            assertEquals("No label with id " + RANDOM_LABEL_ID
                    + " for user with id " + FIRST_USER_ID, entityNotFoundException.getMessage());

            //verify
            verify(labelRepository).existsByIdAndUserId(RANDOM_LABEL_ID, FIRST_USER_ID);
        }
    }
}
