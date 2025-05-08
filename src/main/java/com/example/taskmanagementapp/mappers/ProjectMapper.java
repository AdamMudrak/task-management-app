package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.project.request.CreateProjectDto;
import com.example.taskmanagementapp.dtos.project.request.ProjectStatusDto;
import com.example.taskmanagementapp.dtos.project.response.ProjectDto;
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
    default Project toCreateProject(CreateProjectDto createProjectDto, User user) {
        Project project = new Project();
        project.setName(createProjectDto.name());
        if (createProjectDto.description() != null
                && !createProjectDto.description().isBlank()) {
            project.setDescription(createProjectDto.description());
        }
        project.setStartDate(createProjectDto.startDate());
        project.setEndDate(createProjectDto.endDate());
        project.setOwner(user);
        project.getEmployees().add(user);
        project.getManagers().add(user);
        project.setStatus(Project.Status.INITIATED);
        return project;
    }

    @Mapping(target = "statusDto", ignore = true)
    @Mapping(target = "employeeIds", ignore = true)
    @Mapping(target = "managerIds", ignore = true)
    @Mapping(target = "ownerId", source = "owner.id")
    ProjectDto toProjectDto(Project project);

    List<ProjectDto> toProjectDtoList(List<Project> project);

    @AfterMapping
    default void setStatus(@MappingTarget ProjectDto projectDto, Project project) {
        projectDto.setStatusDto(ProjectStatusDto.valueOf(project.getStatus().name()));
    }

    @AfterMapping
    default void setEmployeeIds(@MappingTarget ProjectDto projectDto, Project project) {
        projectDto.setEmployeeIds(project.getEmployees().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
    }

    @AfterMapping
    default void setManagerIds(@MappingTarget ProjectDto projectDto, Project project) {
        projectDto.setManagerIds(project.getManagers().stream()
                .map(User::getId)
                .collect(Collectors.toSet()));
    }
}
