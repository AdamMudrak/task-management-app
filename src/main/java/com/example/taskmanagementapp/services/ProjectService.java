package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.AssignEmployeeResponseDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectDto createProject(User authenticatedUser, CreateProjectDto createProjectDto);

    List<ProjectDto> getAssignedProjects(Long userId, Pageable pageable);

    List<ProjectDto> getCreatedProjects(Long userId, Pageable pageable);

    List<ProjectDto> getDeletedCreatedProjects(Long userId, Pageable pageable);

    ProjectDto getProjectById(User authenticatedUser, Long projectId) throws ForbiddenException;

    ProjectDto updateProjectById(User authenticatedUser, Long projectId,
                                 UpdateProjectDto updateProjectDto,
                                 ProjectStatusDto projectStatusDto) throws ForbiddenException,
                                                                            ConflictException;

    void deleteProjectById(User authenticatedUser, Long projectId) throws ForbiddenException;

    AssignEmployeeResponseDto assignEmployeeToProject(User authenticatedUser, Long projectId,
                                                      Long employeeId, boolean isNewEmployeeManager)
            throws ForbiddenException;

    ProjectDto acceptAssignmentToProject(HttpServletRequest request);

    ProjectDto removeEmployeeFromProject(User authenticatedUser, Long projectId, Long employeeId)
            throws ForbiddenException;
}
