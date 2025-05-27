package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.project.request.ProjectRequest;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectRequest;
import com.example.taskmanagementapp.dtos.project.response.EmployeeAssignmentResponse;
import com.example.taskmanagementapp.dtos.project.response.ProjectResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ConflictException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(User authenticatedUser, ProjectRequest createProjectDto);

    List<ProjectResponse> getAssignedProjects(Long userId, Pageable pageable);

    List<ProjectResponse> getCreatedProjects(Long userId, Pageable pageable);

    List<ProjectResponse> getDeletedCreatedProjects(Long userId, Pageable pageable);

    ProjectResponse getProjectById(User authenticatedUser,
                                   Long projectId) throws ForbiddenException;

    ProjectResponse updateProjectById(Long authenticatedUserId, Long projectId,
                                      UpdateProjectRequest updateProjectDto,
                                      ProjectStatusDto projectStatusDto) throws ForbiddenException,
                                                                            ConflictException;

    void deleteProjectById(User authenticatedUser, Long projectId) throws ForbiddenException;

    EmployeeAssignmentResponse assignEmployeeToProject(User authenticatedUser, Long projectId,
                                                   Long employeeId, boolean isNewEmployeeManager)
            throws ForbiddenException;

    ProjectResponse acceptAssignmentToProject(HttpServletRequest request);

    ProjectResponse removeEmployeeFromProject(Long authenticatedUserId, Long projectId,
                                              Long employeeId) throws ForbiddenException;
}
