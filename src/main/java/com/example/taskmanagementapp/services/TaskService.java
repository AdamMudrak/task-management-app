package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskRequest;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.request.UpdateTaskRequest;
import com.example.taskmanagementapp.dtos.task.response.TaskResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(User authenticatedUser,
                            TaskRequest createTaskDto,
                            TaskPriorityDto taskPriorityDto) throws ForbiddenException;

    List<TaskResponse> getTasksForProject(User authenticatedUser, Long projectId,
                                          Pageable pageable) throws ForbiddenException;

    TaskResponse getTaskById(User authenticatedUser,
                             Long taskId) throws ForbiddenException;

    TaskResponse updateTask(User authenticatedUser,
                            UpdateTaskRequest updateTaskDto,
                            Long taskId,
                            TaskStatusDto taskStatusDto,
                            TaskPriorityDto taskPriorityDto) throws ForbiddenException;

    void deleteTask(User authenticatedUser,
                    Long taskId) throws ForbiddenException;

    List<TaskResponse> getTasksWithLabel(User user, Long labelId, Pageable pageable);
}
