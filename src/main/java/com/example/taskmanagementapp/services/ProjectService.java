package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.UpdateProjectDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.conflictexpections.ConflictException;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import java.util.List;

public interface ProjectService {
    ProjectDto createProject(User authenticatedUser, CreateProjectDto createProjectDto);

    List<ProjectDto> getProjects(User authenticatedUser);

    ProjectDto getProjectById(User authenticatedUser, Long projectId) throws ForbiddenException;

    ProjectDto updateProjectById(User authenticatedUser, Long projectId,
                                 UpdateProjectDto updateProjectDto) throws ForbiddenException,
                                                                            ConflictException;

    void deleteProjectById(User authenticatedUser, Long projectId) throws ForbiddenException;

    void assignEmployeeToProject(User authenticatedUser, Long projectId, Long employeeId)
                                                                        throws ForbiddenException;

    void removeEmployeeFromProject(User authenticatedUser, Long projectId, Long employeeId)
                                                                        throws ForbiddenException;
}
