package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.ROLE_EMPLOYEE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_MANAGER;
import static com.example.taskmanagementapp.constants.Constants.ROLE_SUPERVISOR;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.ASSIGN_EMPLOYEE;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.DELETED;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECTS;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECT_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECT_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECT_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.REMOVE_EMPLOYEE;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.ProjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PROJECTS)
@Tag(name = PROJECT_API_NAME,
        description = PROJECT_API_DESCRIPTION)
@RequiredArgsConstructor
@Validated
public class ProjectController {
    private final ProjectService projectService;

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping
    ProjectDto createProject(@AuthenticationPrincipal User user,
                             @RequestBody CreateProjectDto createProjectDto) {
        return projectService.createProject(user, createProjectDto);
    }

    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping
    List<ProjectDto> getAllProjects(@AuthenticationPrincipal User user) {
        return projectService.getProjects(user);
    }

    @PreAuthorize(ROLE_SUPERVISOR)
    @GetMapping(DELETED)
    List<ProjectDto> getDeletedProjects(@AuthenticationPrincipal User user)
            throws ForbiddenException {
        return projectService.getDeletedProjects(user);
    }

    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping(PROJECT_ID)
    ProjectDto getProjectById(@AuthenticationPrincipal User user,
                                       @PathVariable @Positive Long projectId)
                                                    throws ForbiddenException {
        return projectService.getProjectById(user, projectId);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PutMapping(PROJECT_ID)
    ProjectDto updateProjectById(@AuthenticationPrincipal User user,
                                 @PathVariable @Positive Long projectId,
                                 @RequestBody UpdateProjectDto updateProjectDto,
                                 @RequestParam(value = "projectStatusDto", required = false)
                                 ProjectStatusDto projectStatusDto)
                                    throws ForbiddenException, ConflictException {
        return projectService.updateProjectById(user, projectId,
                updateProjectDto, projectStatusDto);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @DeleteMapping(PROJECT_ID)
    void deleteProjectById(@AuthenticationPrincipal User user,
                           @PathVariable @Positive Long projectId) throws ForbiddenException {
        projectService.deleteProjectById(user, projectId);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping(ASSIGN_EMPLOYEE)
    ProjectDto assignEmployeeToProject(@AuthenticationPrincipal User user,
                                 @PathVariable @Positive Long projectId,
                                 @PathVariable @Positive Long employeeId) throws ForbiddenException,
                                                                        ConflictException {
        return projectService.assignEmployeeToProject(user, projectId, employeeId);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping(REMOVE_EMPLOYEE)
    ProjectDto removeEmployeeFromProject(@AuthenticationPrincipal User user,
                                 @PathVariable @Positive Long projectId,
                                 @PathVariable @Positive Long employeeId) throws ForbiddenException,
            ConflictException {
        return projectService.removeEmployeeFromProject(user, projectId, employeeId);
    }
}
