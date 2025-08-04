package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.Constants.FIRST_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SECOND_POSITION;
import static com.example.taskmanagementapp.constants.Constants.THIRD_POSITION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CANNOT_DELETE_MANAGER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CANNOT_DELETE_OWNER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.NO_ACCESS_PERMISSION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.NO_ACTION_TOKEN_FOUND;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.NO_OWNER_OR_MANAGER_PERMISSION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.NO_OWNER_PERMISSION;
import static com.example.taskmanagementapp.services.utils.UpdateValueValidatorUtil.areDatesValid;
import static com.example.taskmanagementapp.services.utils.UpdateValueValidatorUtil.areStringsValid;

import com.example.taskmanagementapp.dtos.project.request.ProjectRequest;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectRequest;
import com.example.taskmanagementapp.dtos.project.response.EmployeeAssignmentResponse;
import com.example.taskmanagementapp.dtos.project.response.ProjectResponse;
import com.example.taskmanagementapp.entities.ActionToken;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ActionNotFoundException;
import com.example.taskmanagementapp.exceptions.ConflictException;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.ProjectMapper;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.ProjectService;
import com.example.taskmanagementapp.services.email.AssignmentToProjectEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.services.utils.ProjectAuthorityUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AssignmentToProjectEmailService emailService;
    private final ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    private final JwtStrategy jwtStrategy;
    private final ActionTokenRepository actionTokenRepository;
    private final ProjectAuthorityUtil projectAuthorityUtil;

    @Override
    public ProjectResponse createProject(User user,
                                         ProjectRequest createProjectDto) {
        return projectMapper.toProjectDto(
                projectRepository.save(projectMapper.toCreateProject(createProjectDto, user)));
    }

    @Override
    public List<ProjectResponse> getAssignedProjects(Long userId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllByEmployeeId(userId, pageable)
                        .getContent());
    }

    @Override
    public List<ProjectResponse> getCreatedProjects(Long userId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                        projectRepository.findAllByOwnerId(userId, pageable)
                                .getContent());
    }

    @Override
    public List<ProjectResponse> getDeletedCreatedProjects(Long userId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllByOwnerIdDeleted(userId, pageable)
                        .getContent());
    }

    @Override
    public ProjectResponse getProjectById(Long authenticatedUserId,
                                          Long projectId) throws ForbiddenException {
        Project project = getActiveProjectById(projectId);
        if (projectAuthorityUtil.hasAnyAuthority(projectId, authenticatedUserId)) {
            return projectMapper.toProjectDto(project);
        } else {
            throw new ForbiddenException(NO_ACCESS_PERMISSION);
        }
    }

    @Override
    public ProjectResponse updateProjectById(Long authenticatedUserId,
                                             Long projectId,
                                             UpdateProjectRequest updateProjectDto,
                                             ProjectStatusDto projectStatusDto)
                                        throws ForbiddenException, ConflictException {
        Project project = getActiveProjectById(projectId);
        List<ConflictException> exceptions = new ArrayList<>();
        if (projectAuthorityUtil.hasManagerialAuthority(projectId, authenticatedUserId)) {
            updatePresentField(authenticatedUserId, project,
                    updateProjectDto, projectStatusDto, exceptions);
        } else {
            throw new ForbiddenException(NO_ACCESS_PERMISSION);
        }
        if (exceptions.isEmpty()) {
            return projectMapper.toProjectDto(projectRepository.save(project));
        } else {
            throw accumulateExceptions(exceptions);
        }
    }

    @Override
    public void deleteProjectById(Long authenticatedUserId,
                                  Long projectId) throws ForbiddenException {
        if (!projectRepository.existsByIdNotDeleted(projectId)) {
            throw new EntityNotFoundException("No active project with id " + projectId);
        }
        if (projectRepository.isUserOwner(projectId, authenticatedUserId)) {
            taskRepository.findAllByProjectIdNonDeleted(projectId, Pageable.unpaged())
                    .forEach(task -> commentRepository.deleteAllByTaskId(task.getId()));
            taskRepository.deleteAllByProjectId(projectId);
            projectRepository.deleteById(projectId);
        } else {
            throw new ForbiddenException(NO_OWNER_PERMISSION);
        }
    }

    @Override
    public EmployeeAssignmentResponse assignEmployeeToProject(User user,
                                                              Long projectId,
                                                              Long employeeId,
                                                              boolean isNewEmployeeManager)
                                                        throws ForbiddenException {
        Project project = getActiveProjectById(projectId);
        if (projectAuthorityUtil.hasManagerialAuthority(projectId, user.getId())) {
            User newEmployee = getUserById(employeeId);
            ActionToken actionToken = new ActionToken();
            actionToken.setActionToken("" + projectId + employeeId
                    + isNewEmployeeManager + getActionToken(newEmployee.getEmail()));

            actionTokenRepository.save(actionToken);
            emailService.sendAssignmentEmail(user.getEmail(), newEmployee.getEmail(),
                    project.getName(), actionToken.getActionToken());
            return new EmployeeAssignmentResponse("Employee " + employeeId
                    + " has been invited to project " + projectId);
        } else {
            throw new ForbiddenException(NO_OWNER_OR_MANAGER_PERMISSION);
        }
    }

    @Override
    public ProjectResponse acceptAssignmentToProject(HttpServletRequest request) {
        String token = paramFromHttpRequestUtil.getNamedParameter(request, "shortToken");
        JwtAbstractUtil jwtActionUtil = jwtStrategy.getStrategy(JwtType.ACTION);
        jwtActionUtil.isValidToken(token);
        String actionToken = paramFromHttpRequestUtil.getNamedParameter(request, "actionToken");

        if (!actionTokenRepository.existsByActionToken(actionToken)) {
            throw new ActionNotFoundException(NO_ACTION_TOKEN_FOUND);
        } else {
            actionTokenRepository.deleteByActionToken(actionToken);
        }

        Long projectId = Long.parseLong(actionToken.substring(FIRST_POSITION, SECOND_POSITION));

        Project project = getActiveProjectById(projectId);

        Long assigneeId = Long.parseLong(actionToken.substring(SECOND_POSITION, THIRD_POSITION));
        User assignee = getUserById(assigneeId);

        boolean isNewEmployeeManager = actionToken.contains("true");

        project.getEmployees().add(assignee);
        if (isNewEmployeeManager) {
            project.getManagers().add(assignee);
        }
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @Override
    public ProjectResponse removeEmployeeFromProject(Long userId,
                                                     Long projectId,
                                                     Long employeeId) throws ForbiddenException {
        Project project = getActiveProjectById(projectId);
        if (projectAuthorityUtil.hasManagerialAuthority(projectId, userId)) {
            User removedEmployee = getUserById(employeeId);
            if (project.getOwner().getId().equals(removedEmployee.getId())) {
                throw new ForbiddenException(CANNOT_DELETE_OWNER);
            }
            if (project.getManagers().contains(removedEmployee)
                    && !projectRepository.isUserOwner(projectId, userId)) {
                throw new ForbiddenException(CANNOT_DELETE_MANAGER);
            } else if (project.getManagers().contains(removedEmployee)
                    && projectRepository.isUserOwner(projectId, userId)) {
                project.getManagers().remove(removedEmployee);
            }
            project.getEmployees().remove(removedEmployee);
            return projectMapper.toProjectDto(projectRepository.save(project));
        } else {
            throw new ForbiddenException(
                    "You should be owner or manager of this project "
                            + "to assign new employees and managers");
        }
    }

    private void updatePresentField(Long currentUserId,
                                    Project project,
                                    UpdateProjectRequest updateProjectDto,
                                    ProjectStatusDto projectStatusDto,
                                    List<ConflictException> exceptions) {
        if (areStringsValid(updateProjectDto.name(), project.getName())) {
            project.setName(updateProjectDto.name());
        }
        if (areStringsValid(updateProjectDto.description(),
                project.getDescription())) {
            project.setDescription(updateProjectDto.description());
        }
        if (areDatesValid(updateProjectDto.startDate(), project.getStartDate())) {
            if (updateProjectDto.startDate().isBefore(project.getEndDate())) {
                project.setStartDate(updateProjectDto.startDate());
            } else {
                exceptions.add(new ConflictException("startDate can't be after endDate"));
            }
        }
        if (areDatesValid(updateProjectDto.endDate(), project.getEndDate())) {
            if (updateProjectDto.endDate().isAfter(project.getStartDate())) {
                project.setEndDate(updateProjectDto.endDate());
            } else {
                exceptions.add(new ConflictException("endDate can't be before startDate"));
            }
        }
        if (updateProjectDto.ownerId() != null
                && !updateProjectDto.ownerId().equals(project.getOwner().getId())) {
            if (project.getEmployees().stream().map(User::getId)
                    .noneMatch(updateProjectDto.ownerId()::equals)) {
                exceptions.add(new ConflictException(
                        "Can't assign this user as owner since "
                                + "they are not employees of this project"));
            }
            if (project.getOwner().getId().equals(currentUserId)) {
                User newOwner = getUserById(updateProjectDto.ownerId());
                project.setOwner(newOwner);
                project.getManagers().add(newOwner);
            } else {
                exceptions.add(new ConflictException("Only owner can assign new owner"));
            }
        }
        if (projectStatusDto != null) {
            project.setStatus(Project.Status.valueOf(projectStatusDto.name()));
        }
    }

    private ConflictException accumulateExceptions(List<ConflictException> exceptions) {
        StringBuilder exceptionMessages = new StringBuilder();
        for (ConflictException e : exceptions) {
            exceptionMessages.append(e.getMessage()).append(System.lineSeparator());
        }
        return new ConflictException(exceptionMessages.toString());
    }

    private String getActionToken(String email) {
        return jwtStrategy.getStrategy(JwtType.ACTION).generateToken(email);
    }

    private User getUserById(Long employeeId) {
        return userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                        + employeeId));
    }

    private Project getActiveProjectById(Long projectId) {
        return projectRepository.findByIdNotDeleted(projectId).orElseThrow(
                () -> new EntityNotFoundException("No active project with id " + projectId));
    }
}
