package com.example.taskmanagementapp.dtos.task.request;

import com.example.taskmanagementapp.constants.dtos.TaskDtoConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record UpdateTaskDto(
        @Schema(name = TaskDtoConstants.NAME,
                example = TaskDtoConstants.NAME_EXAMPLE)
        String name,
        @Schema(name = TaskDtoConstants.DESCRIPTION,
                example = TaskDtoConstants.DESCRIPTION_EXAMPLE)
        String description,
        @Schema(name = TaskDtoConstants.DUE_DATE,
                example = TaskDtoConstants.DUE_DATE_EXAMPLE)
        LocalDate dueDate,
        @Schema(name = TaskDtoConstants.PROJECT_ID,
                example = TaskDtoConstants.PROJECT_ID_EXAMPLE)
        @Positive
        Long projectId,
        @Schema(name = TaskDtoConstants.ASSIGNEE_ID,
                example = TaskDtoConstants.ASSIGNEE_ID_EXAMPLE)
        @Positive
        Long assigneeId) {}

