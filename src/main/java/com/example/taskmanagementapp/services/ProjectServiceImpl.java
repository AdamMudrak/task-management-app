package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
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
import com.example.taskmanagementapp.repositories.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public ProjectDto createProject(User user,
                                    CreateProjectDto createProjectDto) {
        Project createProject = projectMapper.toCreateProject(createProjectDto);
        createProject.setOwner(user);
        createProject.setStatus(Project.Status.INITIATED);
        return projectMapper.toProjectDto(projectRepository.save(createProject));
    }

    @Override
    public List<ProjectDto> getProjects(User user) {
        switch (user.getRole().getName()) {
            case ROLE_EMPLOYEE -> {
                return getEmployeeProjects(user.getId());
            }
            case ROLE_MANAGER -> {
                return getManagerProjects(user.getId());
            }
            case ROLE_SUPERVISOR -> {
                return getAllProjects();
            }
            default -> throw new EntityNotFoundException(
                    "No such role " + user.getRole().getName());
        }
    }

    @Override
    public ProjectDto getProjectById(User user,
                                     Long projectId) throws ForbiddenException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        boolean hasAccess = isUserSupervisor(user)
                || isUserOwner(user, project)
                || isUserAssignee(user, project);
        if (hasAccess) {
            return projectMapper.toProjectDto(project);
        } else {
            throw new ForbiddenException("You have no permission to access this project");
        }
    }

    @Override
    public ProjectDto updateProjectById(User user,
                                        Long projectId,
                                        UpdateProjectDto updateProjectDto)
                                        throws ForbiddenException, ConflictException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        boolean hasAccess = isUserSupervisor(user)
                || isUserOwner(user, project);
        List<ConflictException> exceptions = new ArrayList<>();
        if (hasAccess) {
            updatePresentField(project, updateProjectDto, exceptions);
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
        if (isUserSupervisor(user)) {
            projectRepository.deleteById(projectId);
        } else {
            throw new ForbiddenException("To delete a project, contact your supervisor");
        }
    }

    @Override
    public void assignEmployeeToProject(User user,
                                        Long projectId,
                                        Long employeeId) throws ForbiddenException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (isUserSupervisor(user) || isUserOwner(user, project)) {
            User newEmployee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                            + employeeId));
            project.getEmployees().add(newEmployee);
            projectRepository.save(project);
        } else {
            throw new ForbiddenException(
                    "You should have MANAGER or SUPERVISOR access level to assign employee");
        }
    }

    @Override
    public void removeEmployeeFromProject(User user,
                                          Long projectId,
                                          Long employeeId) throws ForbiddenException {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("No project with id " + projectId));
        if (isUserSupervisor(user) || isUserOwner(user, project)) {
            User newEmployee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("No employee with id "
                            + employeeId));
            project.getEmployees().remove(newEmployee);
            projectRepository.save(project);
        } else {
            throw new ForbiddenException(
                    "You should have MANAGER or SUPERVISOR access level to remove employee");
        }
    }

    private List<ProjectDto> getEmployeeProjects(Long authenticatedUserId) {
        return projectMapper.toProjectDtoList(
                projectRepository.findByEmployeeId(authenticatedUserId));
    }

    private List<ProjectDto> getManagerProjects(Long authenticatedUserId) {
        return projectMapper.toProjectDtoList(
                projectRepository.findByOwnerId(authenticatedUserId));
    }

    private List<ProjectDto> getAllProjects() {
        return projectMapper.toProjectDtoList(projectRepository.findAll());
    }

    private boolean isUserSupervisor(User user) {
        return user.getRole().getName().equals(Role.RoleName.ROLE_SUPERVISOR);
    }

    private boolean isUserOwner(User user, Project project) {
        return user.getId().equals(project.getOwner().getId());
    }

    private boolean isUserAssignee(User user, Project project) {
        return project.getEmployees().contains(user);
    }

    private void updatePresentField(Project project,
                                    UpdateProjectDto updateProjectDto,
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
            if (!newOwner.getRole().getName().equals(Role.RoleName.ROLE_MANAGER)
                    && !newOwner.getRole().getName().equals(Role.RoleName.ROLE_SUPERVISOR)) {
                exceptions.add(new ConflictException(
                        "New owner should have MANAGER or SUPERVISOR access level"));
            } else {
                project.setOwner(newOwner);
            }
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
