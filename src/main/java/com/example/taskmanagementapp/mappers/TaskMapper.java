package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskRequest;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.response.TaskResponse;
import com.example.taskmanagementapp.entities.Task;
import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface TaskMapper {
    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "assignee.id", source = "assigneeId")
    Task toCreateTask(TaskRequest createTaskDto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "priority", ignore = true)
    TaskResponse toTaskDto(Task task);

    @AfterMapping
    default void setStatus(Task task, @MappingTarget TaskResponse taskDto) {
        taskDto.setStatus(TaskStatusDto.valueOf(task.getStatus().name()));
    }

    @AfterMapping
    default void setPriority(Task task, @MappingTarget TaskResponse taskDto) {
        taskDto.setPriority(TaskPriorityDto.valueOf(task.getPriority().name()));
    }

    List<TaskResponse> toTaskDtoList(List<Task> tasks);
}
