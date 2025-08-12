package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dto.task.request.TaskRequest;
import com.example.taskmanagementapp.dto.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dto.task.request.UpdateTaskRequest;
import com.example.taskmanagementapp.dto.task.response.TaskResponse;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.ForbiddenException;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(User authenticatedUser,
                            TaskRequest createTaskDto,
                            TaskPriorityDto taskPriorityDto) throws ForbiddenException;

    List<TaskResponse> getTasksForProject(Long authenticatedUserId, Long projectId,
                                          Pageable pageable) throws ForbiddenException;

    TaskResponse getTaskById(Long authenticatedUserId,
                             Long taskId) throws ForbiddenException;

    TaskResponse updateTask(User authenticatedUser,
                            UpdateTaskRequest updateTaskDto,
                            Long taskId,
                            TaskStatusDto taskStatusDto,
                            TaskPriorityDto taskPriorityDto) throws ForbiddenException;

    void deleteTask(Long authenticatedUserId,
                    Long taskId) throws ForbiddenException;

    List<TaskResponse> getTasksWithLabel(Long authenticatedUserId, Long labelId, Pageable pageable);
}
