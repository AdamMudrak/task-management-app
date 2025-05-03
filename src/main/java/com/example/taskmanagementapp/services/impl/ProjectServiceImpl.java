package com.example.taskmanagementapp.services.impl;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.ProjectMapper;
import com.example.taskmanagementapp.repositories.project.ProjectRepository;
import com.example.taskmanagementapp.repositories.task.TaskRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import com.example.taskmanagementapp.security.utils.CheckUserAccessLevelUtil;
import com.example.taskmanagementapp.services.ProjectService;
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
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CheckUserAccessLevelUtil accessLevelUtil;

    @Override
    public ProjectDto createProject(User user,
                                    CreateProjectDto createProjectDto) {
        Project createProject = projectMapper.toCreateProject(createProjectDto);
        createProject.setOwner(user);
        createProject.setStatus(Project.Status.INITIATED);
        return projectMapper.toProjectDto(projectRepository.save(createProject));
    }

    @Override
    public List<ProjectDto> getProjects(User user, Pageable pageable) {
        switch (user.getRole().getName()) {
            case ROLE_USER -> {
                return getEmployeeProjects(user.getId(), pageable);
            }
            case ROLE_ADMIN -> {
                return getManagerProjects(user.getId(), pageable);
            }
            default -> throw new EntityNotFoundException(
                    "No such role " + user.getRole().getName());
        }
    }

    @Override
    public List<ProjectDto> getDeletedProjects(User authenticatedUser, Pageable pageable)
                                                                        throws ForbiddenException {
        if (accessLevelUtil.isUserSupervisor(authenticatedUser)) {
            return projectMapper.toProjectDtoList(
                    projectRepository.findAllDeleted(pageable).getContent());
        } else {
            throw new ForbiddenException("You have no permission to access deleted projects");
        }
    }

    @Override
    public ProjectDto getProjectById(User user,
                                     Long projectId) throws ForbiddenException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (project.isDeleted()) {
            throw new ForbiddenException("Project with id " + projectId + " is deleted");
        }
        if (accessLevelUtil.hasAnyAccess(user, project)) {
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
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        List<ConflictException> exceptions = new ArrayList<>();
        if (accessLevelUtil.hasAdminAccess(user, project)) {
            updatePresentField(project, updateProjectDto, projectStatusDto, exceptions);
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
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));

        if (accessLevelUtil.hasAdminAccess(user, project)) {
            projectRepository.deleteById(projectId);
            taskRepository.deleteAllByProjectId(projectId);
        } else {
            throw new ForbiddenException("You must be supervisor or owner to delete this project");
        }
    }

    @Override
    public ProjectDto assignEmployeeToProject(User user,
                                        Long projectId,
                                        Long employeeId) throws ForbiddenException,
                                                                ConflictException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (accessLevelUtil.hasAdminAccess(user, project)) {
            User newEmployee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                            + employeeId));
            if (!project.getEmployees().contains(newEmployee)) {
                project.getEmployees().add(newEmployee);
            } else {
                throw new ConflictException("Employee " + newEmployee.getId()
                        + " is already part of the project");
            }
            return projectMapper.toProjectDto(projectRepository.save(project));
        } else {
            throw new ForbiddenException(
                    "You should have MANAGER or SUPERVISOR access level to assign employee");
        }
    }

    @Override
    public ProjectDto removeEmployeeFromProject(User user,
                                          Long projectId,
                                          Long employeeId) throws ForbiddenException,
                                                                    ConflictException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (accessLevelUtil.hasAdminAccess(user, project)) {
            User newEmployee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                            + employeeId));
            if (project.getEmployees().contains(newEmployee)) {
                project.getEmployees().remove(newEmployee);
            } else {
                throw new ConflictException("Employee " + newEmployee.getId()
                        + " is not part of the project");
            }
            return projectMapper.toProjectDto(projectRepository.save(project));
        } else {
            throw new ForbiddenException(
                    "You should have MANAGER or SUPERVISOR access level to remove employee");
        }
    }

    private List<ProjectDto> getEmployeeProjects(Long authenticatedUserId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllByEmployeeId(authenticatedUserId, pageable).getContent());
    }

    private List<ProjectDto> getManagerProjects(Long authenticatedUserId, Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllByOwnerId(authenticatedUserId, pageable).getContent());
    }

    private List<ProjectDto> getAllProjects(Pageable pageable) {
        return projectMapper.toProjectDtoList(
                projectRepository.findAllNonDeleted(pageable).getContent());
    }

    private void updatePresentField(Project project,
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
            User newOwner = userRepository.findById(updateProjectDto.ownerId())
                    .orElseThrow(() -> new EntityNotFoundException("No user with id "
                            + updateProjectDto.ownerId()));
            if (!newOwner.getRole().getName().equals(Role.RoleName.ROLE_ADMIN)) {
                exceptions.add(new ConflictException(
                        "New owner should have MANAGER or SUPERVISOR access level"));
            } else {
                project.setOwner(newOwner);
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
