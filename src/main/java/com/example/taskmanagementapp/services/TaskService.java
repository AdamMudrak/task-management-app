package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.task.request.CreateTaskDto;
import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.request.UpdateTaskDto;
import com.example.taskmanagementapp.dtos.task.response.TaskDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskDto createTask(User authenticatedUser,
                       CreateTaskDto createTaskDto,
                       TaskPriorityDto taskPriorityDto) throws ForbiddenException;

    List<TaskDto> getTasksForProject(User authenticatedUser,
                                     Long projectId, Pageable pageable) throws ForbiddenException;

    TaskDto getTaskById(User authenticatedUser,
                        Long taskId) throws ForbiddenException;

    TaskDto updateTask(User authenticatedUser,
                       UpdateTaskDto updateTaskDto,
                       Long taskId,
                       TaskStatusDto taskStatusDto,
                       TaskPriorityDto taskPriorityDto) throws ForbiddenException;

    void deleteTask(User authenticatedUser,
                    Long taskId) throws ForbiddenException;

    List<TaskDto> getTasksWithLabel(User user, Long labelId, Pageable pageable);
}
