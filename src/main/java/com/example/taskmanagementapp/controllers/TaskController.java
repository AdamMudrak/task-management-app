package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.CODE_204;
import static com.example.taskmanagementapp.constants.Constants.CODE_400;
import static com.example.taskmanagementapp.constants.Constants.INVALID_ENTITY_VALUE;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.CREATE_TASK;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.DELETE_TASK_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.GET_TASKS_BY_PROJECT_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.GET_TASK_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.GET_TASK_BY_LABEL_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.SUCCESSFULLY_CREATED_TASK;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.SUCCESSFULLY_DELETED_TASK_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.SUCCESSFULLY_GET_TASKS_BY_PROJECT_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.SUCCESSFULLY_GET_TASK_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.SUCCESSFULLY_GET_TASK_BY_LABEL_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.SUCCESSFULLY_UPDATED_TASK_BY_ID;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.TASKS_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.TASKS_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.TaskControllerConstants.UPDATE_TASK_BY_ID;

import com.example.taskmanagementapp.dtos.task.request.TaskPriorityDto;
import com.example.taskmanagementapp.dtos.task.request.TaskRequest;
import com.example.taskmanagementapp.dtos.task.request.TaskStatusDto;
import com.example.taskmanagementapp.dtos.task.request.UpdateTaskRequest;
import com.example.taskmanagementapp.dtos.task.response.TaskResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@Tag(name = TASKS_API_NAME, description = TASKS_API_DESCRIPTION)
@RequiredArgsConstructor
@Validated
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = CREATE_TASK)
    @ApiResponse(responseCode = CODE_201, description = SUCCESSFULLY_CREATED_TASK)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public TaskResponse createTask(@AuthenticationPrincipal User user,
                       @RequestBody @Valid TaskRequest createTaskDto,
                       @RequestParam TaskPriorityDto taskPriorityDto) throws ForbiddenException {
        return taskService.createTask(user, createTaskDto, taskPriorityDto);
    }

    @Operation(summary = GET_TASKS_BY_PROJECT_ID)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_GET_TASKS_BY_PROJECT_ID)
    @GetMapping("/all-project-tasks/{projectId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<TaskResponse> getTasksByProjectId(@AuthenticationPrincipal User user,
                                          @PathVariable @Positive Long projectId,
                                          @Parameter(example = PAGEABLE_EXAMPLE) Pageable pageable)
            throws ForbiddenException {
        return taskService.getTasksForProject(user.getId(), projectId, pageable);
    }

    @Operation(summary = GET_TASK_BY_ID)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_GET_TASK_BY_ID)
    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TaskResponse getTaskById(@AuthenticationPrincipal User user,
                                    @PathVariable Long taskId) throws ForbiddenException {
        return taskService.getTaskById(user.getId(), taskId);
    }

    @Operation(summary = UPDATE_TASK_BY_ID)
    @ApiResponse(responseCode = CODE_200, description = SUCCESSFULLY_UPDATED_TASK_BY_ID)
    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TaskResponse updateTaskById(@AuthenticationPrincipal User user,
                                       @RequestBody @Valid UpdateTaskRequest updateTaskDto,
                                       @PathVariable @Positive Long taskId,
                                       @RequestParam(value = "taskStatusDto", required = false)
                           TaskStatusDto taskStatusDto,
                                       @RequestParam(value = "taskPriorityDto", required = false)
                           TaskPriorityDto taskPriorityDto)
                            throws ForbiddenException {
        return taskService.updateTask(user, updateTaskDto, taskId, taskStatusDto, taskPriorityDto);
    }

    @Operation(summary = DELETE_TASK_BY_ID)
    @ApiResponse(responseCode = CODE_204, description = SUCCESSFULLY_DELETED_TASK_BY_ID)
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskById(@AuthenticationPrincipal User user,
                        @PathVariable @Positive Long taskId) throws ForbiddenException {
        taskService.deleteTask(user.getId(), taskId);
    }

    @Operation(summary = GET_TASK_BY_LABEL_ID)
    @ApiResponse(responseCode = CODE_204, description = SUCCESSFULLY_GET_TASK_BY_LABEL_ID)
    @GetMapping("/with-label/{labelId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<TaskResponse> getTasksWithLabel(@AuthenticationPrincipal User user,
                                        @PathVariable @Positive Long labelId,
                                        @Parameter(example = PAGEABLE_EXAMPLE) Pageable pageable) {
        return taskService.getTasksWithLabel(user, labelId, pageable);
    }
}
