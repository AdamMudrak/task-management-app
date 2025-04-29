package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.CODE_204;
import static com.example.taskmanagementapp.constants.Constants.CODE_400;
import static com.example.taskmanagementapp.constants.Constants.INVALID_ENTITY_VALUE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_EMPLOYEE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_MANAGER;
import static com.example.taskmanagementapp.constants.Constants.ROLE_SUPERVISOR;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.ADD_EMPLOYEE_TO_PROJECT;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.ASSIGN_EMPLOYEE;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.CREATE_PROJECT;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.DELETED;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.DELETE_EMPLOYEE_FROM_PROJECT;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.DELETE_PROJECT_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.GET_ALL_DELETED_PROJECTS;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.GET_ALL_PROJECTS;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.GET_PROJECT_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECTS;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECT_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECT_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.PROJECT_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.REMOVE_EMPLOYEE;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_ADDED_EMPLOYEE_TO_PROJECT;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_CREATED_PROJECT;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_DELETED_EMPLOYEE_FROM_PROJECT;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_DELETED_PROJECT_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_GET_ALL_DELETED_PROJECTS;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_GET_ALL_PROJECTS;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_GET_PROJECT_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.SUCCESSFULLY_UPDATED_PROJECT_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.ProjectControllerConstants.UPDATE_PROJECT_BY_ID;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import com.example.taskmanagementapp.services.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PROJECTS)
@Tag(name = PROJECT_API_NAME,
        description = PROJECT_API_DESCRIPTION)
@RequiredArgsConstructor
@Validated
public class ProjectController {
    private final ProjectService projectService;

    @Operation(summary = CREATE_PROJECT)
    @ApiResponse(responseCode = CODE_201, description =
            SUCCESSFULLY_CREATED_PROJECT)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProjectDto createProject(@AuthenticationPrincipal User user,
                             @RequestBody CreateProjectDto createProjectDto) {
        return projectService.createProject(user, createProjectDto);
    }

    @Operation(summary = GET_ALL_PROJECTS)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_GET_ALL_PROJECTS)
    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping
    List<ProjectDto> getAllProjects(@AuthenticationPrincipal User user,
                                    @Parameter(example = PAGEABLE_EXAMPLE) Pageable pageable) {
        return projectService.getProjects(user, pageable);
    }

    @Operation(summary = GET_ALL_DELETED_PROJECTS)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_GET_ALL_DELETED_PROJECTS)
    @PreAuthorize(ROLE_SUPERVISOR)
    @GetMapping(DELETED)
    List<ProjectDto> getDeletedProjects(@AuthenticationPrincipal User user,
                                        @Parameter(example = PAGEABLE_EXAMPLE)Pageable pageable)
            throws ForbiddenException {
        return projectService.getDeletedProjects(user, pageable);
    }

    @Operation(summary = GET_PROJECT_BY_ID)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_GET_PROJECT_BY_ID)
    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping(PROJECT_ID)
    ProjectDto getProjectById(@AuthenticationPrincipal User user,
                                       @PathVariable @Positive Long projectId)
                                                    throws ForbiddenException {
        return projectService.getProjectById(user, projectId);
    }

    @Operation(summary = UPDATE_PROJECT_BY_ID)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_PROJECT_BY_ID)
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

    @Operation(summary = DELETE_PROJECT_BY_ID)
    @ApiResponse(responseCode = CODE_204, description =
            SUCCESSFULLY_DELETED_PROJECT_BY_ID)
    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @DeleteMapping(PROJECT_ID)
    void deleteProjectById(@AuthenticationPrincipal User user,
                           @PathVariable @Positive Long projectId) throws ForbiddenException {
        projectService.deleteProjectById(user, projectId);
    }

    @Operation(summary = ADD_EMPLOYEE_TO_PROJECT)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_ADDED_EMPLOYEE_TO_PROJECT)
    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @PostMapping(ASSIGN_EMPLOYEE)
    ProjectDto assignEmployeeToProject(@AuthenticationPrincipal User user,
                                 @PathVariable @Positive Long projectId,
                                 @PathVariable @Positive Long employeeId) throws ForbiddenException,
                                                                        ConflictException {
        return projectService.assignEmployeeToProject(user, projectId, employeeId);
    }

    @Operation(summary = DELETE_EMPLOYEE_FROM_PROJECT)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_DELETED_EMPLOYEE_FROM_PROJECT)
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
