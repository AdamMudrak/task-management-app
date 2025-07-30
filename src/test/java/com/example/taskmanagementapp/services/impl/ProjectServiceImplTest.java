package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.NO_ACCESS_PERMISSION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.NO_OWNER_PERMISSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.project.request.ProjectRequest;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectRequest;
import com.example.taskmanagementapp.dtos.project.response.ProjectResponse;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ConflictException;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.ProjectMapper;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.services.email.AssignmentToProjectEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.services.utils.ProjectAuthorityUtil;
import com.example.taskmanagementapp.testutils.Constants;
import java.time.LocalDate;
import java.util.HashSet;
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
public class ProjectServiceImplTest {
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
    private static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    private static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    private static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);
    private static final long FIRST_USER_ID = 1L;
    private static final long LAST_USER_ID = 2L;
    private static final long FIRST_PROJECT_ID = 1L;
    private static final long ANOTHER_PROJECT_ID = 2L;
    private static final int TEN = 10;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignmentToProjectEmailService emailService;
    @Mock
    private ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    @Mock
    private JwtStrategy jwtStrategy;
    @Mock
    private ActionTokenRepository actionTokenRepository;
    @Mock
    private ProjectAuthorityUtil projectAuthorityUtil;

    @InjectMocks
    private ProjectServiceImpl projectServiceImpl;

    @Nested
    class CreateProject {
        @Test
        void givenValidUserAndDto_whenCreateProject_thenSuccess() {
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

            ProjectRequest projectRequest = new ProjectRequest(
                    PROJECT_NAME,
                    PROJECT_DESCRIPTION,
                    PROJECT_START_DATE,
                    PROJECT_END_DATE);

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

            ProjectResponse projectResponse = ProjectResponse.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .statusDto(ProjectStatusDto.INITIATED)
                    .ownerId(user.getId())
                    .managerIds(Set.of(user.getId()))
                    .employeeIds(Set.of(user.getId()))
                    .build();

            //when
            when(projectMapper.toCreateProject(projectRequest, user)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toProjectDto(project)).thenReturn(projectResponse);

            //then
            assertEquals(projectResponse, projectServiceImpl.createProject(user, projectRequest));
        }
    }

    @Nested
    class GetAssignedProjects {
        @Test
        void givenPageable_whenGetAssignedProjects_thenSuccess() {
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

            PageRequest pageRequest = PageRequest.of(0, 1);

            //since user is owner, they are by default employee and manager of project
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

            ProjectResponse projectResponse = ProjectResponse.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .statusDto(ProjectStatusDto.INITIATED)
                    .ownerId(user.getId())
                    .managerIds(Set.of(user.getId()))
                    .employeeIds(Set.of(user.getId()))
                    .build();

            Page<Project> projects = new PageImpl<>(List.of(project));

            List<ProjectResponse> projectResponses = List.of(projectResponse);

            //when
            when(projectRepository.findAllByEmployeeId(user.getId(), pageRequest))
                    .thenReturn(projects);
            when(projectMapper.toProjectDtoList(projects.getContent()))
                    .thenReturn(projectResponses);

            //then
            assertEquals(projectResponses, projectServiceImpl
                    .getAssignedProjects(user.getId(), pageRequest));

            //verify
            verify(projectRepository, times(1)).findAllByEmployeeId(user.getId(), pageRequest);
            verify(projectMapper, times(1)).toProjectDtoList(projects.getContent());
        }
    }

    @Nested
    class GetCreatedProjects {
        @Test
        void givenPageable_whenGetCreatedProjects_thenSuccess() {
            //given
            PageRequest pageRequest = PageRequest.of(0, 1);

            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user1 = User.builder()
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

            User user2 = User.builder()
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

            //user1 is assigned as owner and manager of this project, whereas user2 - as employee
            Project project = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(user1)
                    .managers(Set.of(user1, user2))
                    .employees(Set.of(user1, user2))
                    .build();

            ProjectResponse projectResponse = ProjectResponse.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .statusDto(ProjectStatusDto.INITIATED)
                    .ownerId(user1.getId())
                    .managerIds(Set.of(user1.getId(), user2.getId()))
                    .employeeIds(Set.of(user1.getId(), user2.getId()))
                    .build();

            Page<Project> projects = new PageImpl<>(List.of(project));
            List<ProjectResponse> projectResponses = List.of(projectResponse);

            Page<Project> emptyProjects = new PageImpl<>(List.of());
            List<ProjectResponse> emptyProjectResponses = List.of();

            //when
            when(projectRepository.findAllByOwnerId(user1.getId(), pageRequest))
                    .thenReturn(projects);
            when(projectMapper.toProjectDtoList(projects.getContent()))
                    .thenReturn(projectResponses);

            when(projectRepository.findAllByOwnerId(user2.getId(), pageRequest))
                    .thenReturn(emptyProjects);
            when(projectMapper.toProjectDtoList(emptyProjects.getContent()))
                    .thenReturn(emptyProjectResponses);

            //then
            assertEquals(projectResponses, projectServiceImpl
                    .getCreatedProjects(user1.getId(), pageRequest));

            assertEquals(emptyProjectResponses, projectServiceImpl
                    .getCreatedProjects(user2.getId(), pageRequest));

            //verify
            verify(projectRepository, times(1)).findAllByOwnerId(user1.getId(), pageRequest);
            verify(projectMapper, times(1)).toProjectDtoList(projects.getContent());
            verify(projectRepository, times(1)).findAllByOwnerId(user2.getId(), pageRequest);
            verify(projectMapper, times(1)).toProjectDtoList(emptyProjects.getContent());
        }
    }

    @Nested
    class GetDeletedCreatedProjects {
        @Test
        void givenPageable_whenGetDeletedProjects_thenSuccess() {
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

            PageRequest pageRequest = PageRequest.of(0, 1);

            Project deletedProject = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.COMPLETED)
                    .isDeleted(true)
                    .owner(user)
                    .managers(Set.of(user))
                    .employees(Set.of(user))
                    .build();

            ProjectResponse deletedProjectResponse = ProjectResponse.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .statusDto(ProjectStatusDto.COMPLETED)
                    .ownerId(user.getId())
                    .managerIds(Set.of(user.getId()))
                    .employeeIds(Set.of(user.getId()))
                    .build();

            Page<Project> projects = new PageImpl<>(List.of(deletedProject));
            List<ProjectResponse> projectResponses = List.of(deletedProjectResponse);

            //when
            when(projectRepository.findAllByOwnerIdDeleted(user.getId(), pageRequest))
                    .thenReturn(projects);
            when(projectMapper.toProjectDtoList(projects.getContent()))
                    .thenReturn(projectResponses);

            //then
            assertEquals(projectResponses, projectServiceImpl
                    .getDeletedCreatedProjects(user.getId(), pageRequest));

            //verify
            verify(projectRepository, times(1)).findAllByOwnerIdDeleted(user.getId(), pageRequest);
            verify(projectMapper, times(1)).toProjectDtoList(projects.getContent());
        }
    }

    @Nested
    class GetProjectById {
        @Test
        void givenDeletedProjectId_whenGetProjectById_thenFail() {
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

            Project expectedProject = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .isDeleted(true)
                    .owner(user)
                    .managers(Set.of(user))
                    .employees(Set.of(user))
                    .build();

            //when
            when(projectRepository.findByIdNotDeleted(FIRST_PROJECT_ID))
                    .thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                        () -> projectServiceImpl.getProjectById(user.getId(),
                                expectedProject.getId()));
            assertEquals("No active project with id "
                    + expectedProject.getId(), entityNotFoundException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(expectedProject.getId());
        }

        @Test
        void givenProjectId_whenGetProjectById_thenSuccess() throws ForbiddenException {
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

            Project expectedProject = Project.builder()
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

            ProjectResponse projectResponse = ProjectResponse.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .statusDto(ProjectStatusDto.INITIATED)
                    .ownerId(user.getId())
                    .managerIds(Set.of(user.getId()))
                    .employeeIds(Set.of(user.getId()))
                    .build();

            //when
            when(projectRepository.findByIdNotDeleted(expectedProject.getId()))
                    .thenReturn(Optional.of(expectedProject));
            when(projectAuthorityUtil.hasAnyAuthority(expectedProject.getId(), user.getId()))
                    .thenReturn(true);
            when(projectMapper.toProjectDto(expectedProject)).thenReturn(projectResponse);

            //then
            assertEquals(projectResponse,
                    projectServiceImpl.getProjectById(user.getId(), expectedProject.getId()));

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(expectedProject.getId());
            verify(projectAuthorityUtil, times(1)).hasAnyAuthority(
                    expectedProject.getId(), user.getId());
            verify(projectMapper, times(1)).toProjectDto(expectedProject);
        }

        @Test
        void givenAnotherUserProjectId_whenGetProjectById_thenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user1 = User.builder()
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

            User user2 = User.builder()
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

            Project expectedProject = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(user1)
                    .managers(Set.of(user1))
                    .employees(Set.of(user1))
                    .build();

            //when
            when(projectRepository.findByIdNotDeleted(expectedProject.getId()))
                    .thenReturn(Optional.of(expectedProject));
            when(projectAuthorityUtil.hasAnyAuthority(expectedProject.getId(), user2.getId()))
                    .thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> projectServiceImpl.getProjectById(
                            user2.getId(), expectedProject.getId()));
            assertEquals(NO_ACCESS_PERMISSION,
                    forbiddenException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(expectedProject.getId());
            verify(projectAuthorityUtil, times(1)).hasAnyAuthority(
                    expectedProject.getId(), user2.getId());
        }
    }

    @Nested
    class UpdateProjectById {
        @Test
        void givenProjectId_whenUpdateProjectById_thenSuccess()
                throws ForbiddenException, ConflictException {
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

            User newOwner = User.builder()
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

            Project expectedProject = Project.builder()
                    .id(FIRST_PROJECT_ID)
                    .name(PROJECT_NAME)
                    .description(PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(authenticatedUser)
                    .managers(new HashSet<>())
                    .employees(new HashSet<>())
                    .build();
            expectedProject.getManagers().add(authenticatedUser);
            expectedProject.getEmployees().add(authenticatedUser);
            expectedProject.getEmployees().add(newOwner);

            UpdateProjectRequest updateProjectRequest = new UpdateProjectRequest(
                    ANOTHER_PROJECT_NAME,
                    ANOTHER_PROJECT_DESCRIPTION,
                    PROJECT_START_DATE.plusDays(TEN),
                    PROJECT_END_DATE.plusDays(TEN),
                    newOwner.getId());

            ProjectStatusDto newProjectStatusDto = ProjectStatusDto.IN_PROGRESS;

            ProjectResponse expectedProjectResponse = ProjectResponse.builder()
                    .id(expectedProject.getId())
                    .name(updateProjectRequest.name())
                    .description(updateProjectRequest.description())
                    .startDate(updateProjectRequest.startDate())
                    .endDate(updateProjectRequest.endDate())
                    .statusDto(newProjectStatusDto)
                    .ownerId(newOwner.getId())
                    .employeeIds(Set.of(authenticatedUser.getId(), newOwner.getId()))
                    .managerIds(Set.of(authenticatedUser.getId(), newOwner.getId()))
                    .build();

            //when
            when(projectRepository.findByIdNotDeleted(expectedProject.getId()))
                    .thenReturn(Optional.of(expectedProject));
            when(projectAuthorityUtil.hasManagerialAuthority(expectedProject.getId(),
                    authenticatedUser.getId())).thenReturn(true);
            when(userRepository.findById(newOwner.getId())).thenReturn(Optional.of(newOwner));
            when(projectRepository.save(expectedProject)).thenReturn(expectedProject);
            when(projectMapper.toProjectDto(expectedProject))
                    .thenReturn(expectedProjectResponse);

            //then
            assertEquals(expectedProjectResponse, projectServiceImpl.updateProjectById(
                    authenticatedUser.getId(), expectedProject.getId(),
                    updateProjectRequest, newProjectStatusDto));

            //verify
            verify(projectRepository, times(1))
                    .findByIdNotDeleted(expectedProject.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(expectedProject.getId(), authenticatedUser.getId());
            verify(userRepository, times(1)).findById(newOwner.getId());
            verify(projectRepository, times(1)).save(expectedProject);
            verify(projectMapper, times(1)).toProjectDto(expectedProject);
        }

        @Test
        void givenForbiddenProjectId_whenUpdateProjectById_thenFail() {
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

            Project expectedProject = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(owner)
                    .managers(Set.of(owner))
                    .employees(Set.of(owner))
                    .build();

            UpdateProjectRequest updateProjectRequest = new UpdateProjectRequest(
                    ANOTHER_PROJECT_NAME,
                    ANOTHER_PROJECT_DESCRIPTION,
                    PROJECT_START_DATE.plusDays(TEN),
                    PROJECT_END_DATE.plusDays(TEN),
                    LAST_USER_ID);
            ProjectStatusDto newProjectStatusDto = ProjectStatusDto.IN_PROGRESS;
            //when
            when(projectRepository.findByIdNotDeleted(expectedProject.getId()))
                    .thenReturn(Optional.of(expectedProject));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    expectedProject.getId(), authenticatedUser.getId())).thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> projectServiceImpl.updateProjectById(
                            authenticatedUser.getId(), expectedProject.getId(),
                            updateProjectRequest, newProjectStatusDto));
            assertEquals(NO_ACCESS_PERMISSION,
                    forbiddenException.getMessage());

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(expectedProject.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(expectedProject.getId(), authenticatedUser.getId());
        }

        @Test
        void givenProjectIdAndBadDto_whenUpdateProjectById_thenFail() {
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

            Project expectedProject = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(owner)
                    .managers(Set.of(owner, authenticatedUser))
                    .employees(Set.of(owner, authenticatedUser))
                    .build();

            UpdateProjectRequest updateProjectRequest = new UpdateProjectRequest(
                    ANOTHER_PROJECT_NAME,
                    ANOTHER_PROJECT_DESCRIPTION,
                    PROJECT_START_DATE.plusYears(TEN),
                    PROJECT_END_DATE.minusYears(TEN),
                    FIRST_USER_ID);
            ProjectStatusDto newProjectStatusDto = ProjectStatusDto.IN_PROGRESS;

            //when
            when(projectRepository.findByIdNotDeleted(expectedProject.getId()))
                    .thenReturn(Optional.of(expectedProject));
            when(projectAuthorityUtil.hasManagerialAuthority(
                    expectedProject.getId(), authenticatedUser.getId())).thenReturn(true);

            //then
            ConflictException conflictException = assertThrows(ConflictException.class, () ->
                    projectServiceImpl.updateProjectById(
                            authenticatedUser.getId(), expectedProject.getId(),
                            updateProjectRequest, newProjectStatusDto));
            assertTrue(conflictException.getMessage().contains("startDate can't be after endDate"));
            assertTrue(conflictException.getMessage()
                    .contains("endDate can't be before startDate"));
            assertTrue(conflictException.getMessage().contains("Only owner can assign new owner"));

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(expectedProject.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(expectedProject.getId(), authenticatedUser.getId());
        }
    }

    @Nested
    class DeleteProjectById {
        @Test
        void givenProjectId_whenDeleteProjectById_thenSuccess() throws ForbiddenException {
            //given
            Long authenticatedUserId = Constants.FIRST_USER_ID;
            Long projectId = Constants.FIRST_PROJECT_ID;

            //when
            when(projectRepository.existsByIdNotDeleted(projectId)).thenReturn(true);
            when(projectRepository.isUserOwner(projectId, authenticatedUserId)).thenReturn(true);
            when(taskRepository.findAllByProjectIdNonDeleted(projectId, Pageable.unpaged()))
                    .thenReturn(new PageImpl<>(List.of()));

            //then
            projectServiceImpl.deleteProjectById(authenticatedUserId, projectId);
        }

        @Test
        void givenAlienProjectId_whenDeleteProjectById_thenFail() {
            //given
            Long authenticatedUserId = Constants.FIRST_USER_ID;
            Long projectId = Constants.FIRST_PROJECT_ID;

            //when
            when(projectRepository.existsByIdNotDeleted(projectId)).thenReturn(true);
            when(projectRepository.isUserOwner(projectId, authenticatedUserId)).thenReturn(false);

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> projectServiceImpl.deleteProjectById(authenticatedUserId, projectId));
            assertEquals(NO_OWNER_PERMISSION, forbiddenException.getMessage());

            //verify
            verify(projectRepository, times(1)).existsByIdNotDeleted(projectId);
            verify(projectRepository, times(1)).isUserOwner(projectId, authenticatedUserId);
        }

        @Test
        void givenNotRealProjectId_whenDeleteProjectById_thenFail() {
            //given
            Long authenticatedUserId = Constants.FIRST_USER_ID;
            Long projectId = Constants.RANDOM_PROJECT_ID;

            //when
            when(projectRepository.existsByIdNotDeleted(projectId)).thenReturn(false);

            //then
            EntityNotFoundException entityNotFoundException = assertThrows(
                    EntityNotFoundException.class,
                    () -> projectServiceImpl.deleteProjectById(authenticatedUserId, projectId));
            assertEquals("No active project with id " + projectId,
                    entityNotFoundException.getMessage());

            //verify
            verify(projectRepository, times(1)).existsByIdNotDeleted(projectId);
        }
    }

    @Nested
    class RemoveEmployeeFromProject {
        @Test
        void givenValidArguments_whenRemoveEmployeeFromProject_thenSuccess()
                throws ForbiddenException {
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

            Project expectedProject = Project.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .status(Project.Status.INITIATED)
                    .owner(authenticatedUser)
                    .managers(new HashSet<>())
                    .employees(new HashSet<>())
                    .build();
            expectedProject.getEmployees().add(authenticatedUser);
            expectedProject.getEmployees().add(assignee);
            expectedProject.getManagers().add(authenticatedUser);
            expectedProject.getManagers().add(assignee);
            
            ProjectResponse projectResponse = ProjectResponse.builder()
                    .id(ANOTHER_PROJECT_ID)
                    .name(ANOTHER_PROJECT_NAME)
                    .description(ANOTHER_PROJECT_DESCRIPTION)
                    .startDate(PROJECT_START_DATE)
                    .endDate(PROJECT_END_DATE)
                    .statusDto(ProjectStatusDto.INITIATED)
                    .ownerId(authenticatedUser.getId())
                    .managerIds(Set.of(authenticatedUser.getId()))
                    .employeeIds(Set.of(authenticatedUser.getId()))
                    .build();

            //when
            when(projectRepository.findByIdNotDeleted(expectedProject.getId()))
                    .thenReturn(Optional.of(expectedProject));
            when(projectAuthorityUtil.hasManagerialAuthority(expectedProject.getId(),
                    authenticatedUser.getId()))
                    .thenReturn(true);
            when(projectRepository.isUserOwner(expectedProject.getId(), authenticatedUser.getId()))
                    .thenReturn(true);
            when(userRepository.findById(assignee.getId())).thenReturn(Optional.of(assignee));
            when(projectRepository.save(expectedProject)).thenReturn(expectedProject);
            when(projectMapper.toProjectDto(expectedProject)).thenReturn(projectResponse);

            //then
            assertEquals(projectResponse, projectServiceImpl
                    .removeEmployeeFromProject(
                            authenticatedUser.getId(),
                            expectedProject.getId(),
                            assignee.getId()));

            //verify
            verify(projectRepository, times(1)).findByIdNotDeleted(expectedProject.getId());
            verify(projectAuthorityUtil, times(1))
                    .hasManagerialAuthority(expectedProject.getId(), authenticatedUser.getId());
            verify(userRepository, times(1)).findById(assignee.getId());
            verify(projectRepository, times(2))
                    .isUserOwner(expectedProject.getId(), authenticatedUser.getId());
            verify(projectRepository, times(1)).save(expectedProject);
            verify(projectMapper, times(1)).toProjectDto(expectedProject);
        }
    }
}
