package com.example.taskmanagementapp.dtos.task.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.TaskDtoConstants;
import com.example.taskmanagementapp.validation.date.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateTaskDto(
        @Schema(name = TaskDtoConstants.NAME,
                example = TaskDtoConstants.NAME_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        String name,
        @Schema(name = TaskDtoConstants.DESCRIPTION,
                example = TaskDtoConstants.DESCRIPTION_EXAMPLE)
        String description,
        @Schema(name = TaskDtoConstants.DUE_DATE,
                example = TaskDtoConstants.DUE_DATE_EXAMPLE,
                requiredMode = REQUIRED)
        @NotNull
        @Date
        LocalDate dueDate,
        @Schema(name = TaskDtoConstants.PROJECT_ID,
                example = TaskDtoConstants.PROJECT_ID_EXAMPLE,
                requiredMode = REQUIRED)
        @NotNull
        @Positive
        Long projectId,
        @Schema(name = TaskDtoConstants.ASSIGNEE_ID,
                example = TaskDtoConstants.ASSIGNEE_ID_EXAMPLE,
                requiredMode = REQUIRED)
        @NotNull
        @Positive
        Long assigneeId){}
