package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ASSIGNEE_ID;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.IS_NEW_MANAGER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PROJECT_ID;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.AssignEmployeeResponseDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.ProjectMapper;
import com.example.taskmanagementapp.repositories.comment.CommentRepository;
import com.example.taskmanagementapp.repositories.project.ProjectRepository;
import com.example.taskmanagementapp.repositories.task.TaskRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.services.ProjectService;
import com.example.taskmanagementapp.services.email.AssignmentToProjectEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AssignmentToProjectEmailService emailService;
    private final ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    private final JwtStrategy jwtStrategy;

    @Override
    public ProjectDto createProject(User user,
                                    CreateProjectDto createProjectDto) {
        return projectMapper.toProjectDto(
                projectRepository.save(projectMapper.toCreateProject(createProjectDto, user)));
    }

    @Override
    public List<ProjectDto> getAssignedProjects(Long userId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllByEmployeeId(userId, pageable)
                        .getContent());
    }

    @Override
    public List<ProjectDto> getCreatedProjects(Long userId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                        projectRepository.findAllByOwnerId(userId, pageable)
                                .getContent());
    }

    @Override
    public List<ProjectDto> getDeletedCreatedProjects(Long userId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllByOwnerIdDeleted(userId, pageable)
                        .getContent());
    }

    @Override
    public ProjectDto getProjectById(User user,
                                     Long projectId) throws ForbiddenException {
        Project project = projectRepository.findByIdNotDeleted(projectId).orElseThrow(
                () -> new EntityNotFoundException("No active project with id " + projectId));
        if (projectRepository.isUserEmployee(projectId, user.getId())
                || projectRepository.isUserOwner(projectId, user.getId())) {
            return projectMapper.toProjectDto(project);
        } else {
            throw new ForbiddenException("You have no permission to access this project");
        }
    }

    @Override
    public ProjectDto updateProjectById(User user,
                                        Long projectId,
                                        UpdateProjectDto updateProjectDto,
                                        ProjectStatusDto projectStatusDto)
                                        throws ForbiddenException, ConflictException {
        Project project = projectRepository.findByIdNotDeleted(projectId).orElseThrow(
                () -> new EntityNotFoundException("No active project with id " + projectId));
        List<ConflictException> exceptions = new ArrayList<>();
        if (projectRepository.isUserManager(projectId, user.getId())
                || projectRepository.isUserOwner(projectId, user.getId())) {
            updatePresentField(user.getId(), project,
                    updateProjectDto, projectStatusDto, exceptions);
        } else {
            throw new ForbiddenException("You have no permission to access this project");
        }
        if (exceptions.isEmpty()) {
            return projectMapper.toProjectDto(projectRepository.save(project));
        } else {
            throw accumulateExceptions(exceptions);
        }
    }

    @Override
    public void deleteProjectById(User user,
                                  Long projectId) throws ForbiddenException {
        if (!projectRepository.existsByIdNotDeleted(projectId)) {
            throw new EntityNotFoundException("No active project with id " + projectId);
        }
        if (projectRepository.isUserOwner(projectId, user.getId())) {
            taskRepository.findAllByProjectIdNonDeleted(projectId, Pageable.unpaged())
                    .forEach(task -> commentRepository.deleteAllByTaskId(task.getId()));
            taskRepository.deleteAllByProjectId(projectId);
            projectRepository.deleteById(projectId);
        } else {
            throw new ForbiddenException("You must be owner to delete this project");
        }
    }

    @Override
    public AssignEmployeeResponseDto assignEmployeeToProject(User user,
                                                             Long projectId,
                                                             Long employeeId,
                                                             boolean isNewEmployeeManager)
                                                        throws ForbiddenException {
        if (user.getId().equals(employeeId)) {
            throw new ForbiddenException("You cannot assign yourself to a project");
        }
        Project project = projectRepository.findByIdNotDeleted(projectId).orElseThrow(
                () -> new EntityNotFoundException("No active project with id " + projectId));
        if (projectRepository.isUserManager(projectId, user.getId())
                || projectRepository.isUserOwner(projectId, user.getId())) {
            User newEmployee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                            + employeeId));
            emailService.sendChangeEmail(user.getEmail(), newEmployee.getEmail(), project.getName(),
                    projectId, employeeId, isNewEmployeeManager);
            return new AssignEmployeeResponseDto("Employee " + employeeId
                    + " has been invited to project " + projectId);
        } else {
            throw new ForbiddenException(
                    "You should be owner or manager of this project "
                            + "to assign new employees and managers");
        }
    }

    @Override
    public ProjectDto acceptAssignmentToProject(HttpServletRequest request) {
        String token = paramFromHttpRequestUtil.parseRandomParameterAndToken(request);
        JwtAbstractUtil jwtActionUtil = jwtStrategy.getStrategy(ACTION);
        jwtActionUtil.isValidToken(token);

        Long projectId = Long.valueOf(paramFromHttpRequestUtil
                .getNamedParameter(request, PROJECT_ID));
        Project project = projectRepository.findByIdNotDeleted(projectId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No active project with id " + projectId));

        Long assigneeId = Long.valueOf(paramFromHttpRequestUtil
                .getNamedParameter(request, ASSIGNEE_ID));
        User assignee = userRepository.findById(assigneeId).orElseThrow(
                () -> new EntityNotFoundException("No user with id " + assigneeId));

        boolean isNewEmployeeManager = Boolean.parseBoolean(
                paramFromHttpRequestUtil.getNamedParameter(request, IS_NEW_MANAGER));

        project.getEmployees().add(assignee);
        if (isNewEmployeeManager) {
            project.getManagers().add(assignee);
        }
        return projectMapper.toProjectDto(projectRepository.save(project));
    }

    @Override
    public ProjectDto removeEmployeeFromProject(User user,
                                          Long projectId,
                                          Long employeeId) throws ForbiddenException {
        if (user.getId().equals(employeeId)) {
            throw new ForbiddenException("You cannot remove yourself from a project");
        }
        Project project = projectRepository.findByIdNotDeleted(projectId).orElseThrow(
                () -> new EntityNotFoundException("No active project with id " + projectId));
        if (projectRepository.isUserManager(projectId, user.getId())
                || projectRepository.isUserOwner(projectId, user.getId())) {
            User removedEmployee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                            + employeeId));
            if (project.getManagers().contains(removedEmployee)) {
                if (!projectRepository.isUserOwner(projectId, user.getId())) {
                    throw new ForbiddenException("Only project owner can delete managers");
                }
            }
            project.getEmployees().remove(removedEmployee);
            project.getManagers().remove(removedEmployee);
            return projectMapper.toProjectDto(projectRepository.save(project));
        } else {
            throw new ForbiddenException(
                    "You should be owner or manager of this project "
                            + "to assign new employees and managers");
        }
    }

    private void updatePresentField(Long currentUserId,
                                    Project project,
                                    UpdateProjectDto updateProjectDto,
                                    ProjectStatusDto projectStatusDto,
                                    List<ConflictException> exceptions) {
        if (updateProjectDto.name() != null
                && !updateProjectDto.name().isBlank()
                && !updateProjectDto.name().equals(project.getName())) {
            project.setName(updateProjectDto.name());
        }
        if (updateProjectDto.description() != null
                && !updateProjectDto.description().isBlank()
                && !updateProjectDto.description().equals(project.getDescription())) {
            project.setDescription(updateProjectDto.description());
        }
        if (updateProjectDto.startDate() != null
                && !updateProjectDto.startDate().isEqual(project.getStartDate())) {
            if (updateProjectDto.startDate().isBefore(project.getEndDate())) {
                project.setStartDate(updateProjectDto.startDate());
            } else {
                exceptions.add(new ConflictException("startDate can't be after endDate"));
            }
        }
        if (updateProjectDto.endDate() != null
                && !updateProjectDto.endDate().isEqual(project.getEndDate())) {
            if (updateProjectDto.endDate().isAfter(project.getStartDate())) {
                project.setEndDate(updateProjectDto.endDate());
            } else {
                exceptions.add(new ConflictException("endDate can't be before startDate"));
            }
        }
        if (updateProjectDto.ownerId() != null
                && !updateProjectDto.ownerId().equals(project.getOwner().getId())) {
            if (project.getOwner().getId().equals(currentUserId)) {
                User newOwner = userRepository.findById(updateProjectDto.ownerId())
                        .orElseThrow(() -> new EntityNotFoundException("No user with id "
                                + updateProjectDto.ownerId()));
                project.setOwner(newOwner);
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
}
