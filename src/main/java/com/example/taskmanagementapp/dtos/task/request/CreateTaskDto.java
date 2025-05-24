package com.example.taskmanagementapp.dtos.task.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.validation.date.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateTaskDto(
        @Schema(name = "name",
                example = "Project Omega",
                requiredMode = REQUIRED)
        @NotBlank
        String name,
        @Schema(name = "description",
                example = "Omega description")
        String description,
        @Schema(name = "dueDate",
                example = "2026-01-01",
                requiredMode = REQUIRED)
        @NotNull
        @Date
        LocalDate dueDate,
        @Schema(name = "projectId",
                example = "1",
                requiredMode = REQUIRED)
        @NotNull
        @Positive
        Long projectId,
        @Schema(name = "assigneeId",
                example = "1",
                requiredMode = REQUIRED)
        @NotNull
        @Positive
        Long assigneeId){}
