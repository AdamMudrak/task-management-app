package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.project.request.ProjectRequest;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectResponse;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.User;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "createProjectDto.name")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "startDate", source = "createProjectDto.startDate")
    @Mapping(target = "owner", source = "user")
    @Mapping(target = "status",
            expression = "java(com.example.taskmanagementapp.entities.Project.Status.INITIATED)")
    Project toCreateProject(ProjectRequest createProjectDto, User user);

    @AfterMapping
    default void setEmployeesAndManagers(@MappingTarget Project project,
                                         User user,
                                         ProjectRequest createProjectDto) {
        if (createProjectDto.description() != null
                && !createProjectDto.description().isBlank()) {
            project.setDescription(createProjectDto.description());
        }
        project.getEmployees().add(user);
        project.getManagers().add(user);
    }

    @Mapping(target = "statusDto", ignore = true)
    @Mapping(target = "employeeIds", ignore = true)
    @Mapping(target = "managerIds", ignore = true)
    @Mapping(target = "ownerId", source = "owner.id")
    ProjectResponse toProjectDto(Project project);

    List<ProjectResponse> toProjectDtoList(List<Project> project);

    @AfterMapping
    default void setStatus(@MappingTarget ProjectResponse projectDto, Project project) {
        projectDto.setStatusDto(ProjectStatusDto.valueOf(project.getStatus().name()));
    }

    @AfterMapping
    default void setEmployeeIds(@MappingTarget ProjectResponse projectDto, Project project) {
        projectDto.setEmployeeIds(project.getEmployees().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
    }

    @AfterMapping
    default void setManagerIds(@MappingTarget ProjectResponse projectDto, Project project) {
        projectDto.setManagerIds(project.getManagers().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
    }
}
