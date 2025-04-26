package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.ROLE_EMPLOYEE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_MANAGER;
import static com.example.taskmanagementapp.constants.Constants.ROLE_SUPERVISOR;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.ProjectService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping
    ProjectDto createProject(@AuthenticationPrincipal User user,
                             CreateProjectDto createProjectDto) {
        return projectService.createProject(user, createProjectDto);
    }

    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping
    List<ProjectDto> getAllProjects(@AuthenticationPrincipal User user) {
        return projectService.getProjects(user);
    }

    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping("/{projectId}")
    ProjectDto getProjectById(@AuthenticationPrincipal User user,
                                       @PathVariable Long projectId) throws ForbiddenException {
        return projectService.getProjectById(user, projectId);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PutMapping("/{projectId}")
    ProjectDto updateProjectById(@AuthenticationPrincipal User user,
                                 @PathVariable Long projectId,
                                 @RequestBody UpdateProjectDto updateProjectDto,
                                 @RequestParam(value = "projectStatusDto", required = false)
                                 ProjectStatusDto projectStatusDto)
                                    throws ForbiddenException, ConflictException {
        return projectService.updateProjectById(user, projectId,
                updateProjectDto, projectStatusDto);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @DeleteMapping("/{projectId}")
    void deleteProjectById(@AuthenticationPrincipal User user,
                           @PathVariable Long projectId) throws ForbiddenException {
        projectService.deleteProjectById(user, projectId);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping("assign-employee/{projectId}/{employeeId}")
    void assignEmployeeToProject(@AuthenticationPrincipal User user,
                                 @PathVariable Long projectId,
                                 @PathVariable Long employeeId) throws ForbiddenException,
                                                                        ConflictException {
        projectService.assignEmployeeToProject(user, projectId, employeeId);
    }

    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping("remove-employee/{projectId}/{employeeId}")
    void removeEmployeeFromProject(@AuthenticationPrincipal User user,
                                 @PathVariable Long projectId,
                                 @PathVariable Long employeeId) throws ForbiddenException,
            ConflictException {
        projectService.removeEmployeeFromProject(user, projectId, employeeId);
    }
}
