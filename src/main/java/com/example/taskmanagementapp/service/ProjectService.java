package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.project.request.ProjectRequest;
import com.example.taskmanagementapp.dto.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dto.project.request.UpdateProjectRequest;
import com.example.taskmanagementapp.dto.project.response.EmployeeAssignmentResponse;
import com.example.taskmanagementapp.dto.project.response.ProjectResponse;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.ConflictException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(User authenticatedUser, ProjectRequest createProjectDto);

    List<ProjectResponse> getAssignedProjects(Long userId, Pageable pageable);

    List<ProjectResponse> getCreatedProjects(Long userId, Pageable pageable);

    List<ProjectResponse> getDeletedCreatedProjects(Long userId, Pageable pageable);

    ProjectResponse getProjectById(Long authenticatedUserId,
                                   Long projectId) throws ForbiddenException;

    ProjectResponse updateProjectById(Long authenticatedUserId, Long projectId,
                                      UpdateProjectRequest updateProjectDto,
                                      ProjectStatusDto projectStatusDto) throws ForbiddenException,
                                                                            ConflictException;

    void deleteProjectById(Long authenticatedUserId, Long projectId) throws ForbiddenException;

    EmployeeAssignmentResponse assignEmployeeToProject(User authenticatedUser, Long projectId,
                                                   Long employeeId, boolean isNewEmployeeManager)
            throws ForbiddenException;

    ProjectResponse acceptAssignmentToProject(HttpServletRequest request);

    ProjectResponse removeEmployeeFromProject(Long authenticatedUserId, Long projectId,
                                              Long employeeId) throws ForbiddenException;
}
