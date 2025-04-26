package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import java.util.List;

public interface ProjectService {
    ProjectDto createProject(Long authenticatedUserId, CreateProjectDto createProjectDto);

    List<ProjectDto> getProjects(Long authenticatedUserId);

    ProjectDto getProjectById(Long authenticatedUserId, Long projectId);

    ProjectDto updateProjectById(Long authenticatedUserId, Long projectId);

    void deleteProjectById(Long authenticatedUserId, Long projectId);

    void assignEmployeeToProject(Long authenticatedUserId, Long projectId, Long employeeId);

    void removeEmployeeFromProject(Long authenticatedUserId, Long projectId, Long employeeId);
}
