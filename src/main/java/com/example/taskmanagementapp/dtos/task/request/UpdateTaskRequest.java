package com.example.taskmanagementapp.dtos.task.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record UpdateTaskRequest(
        @Schema(name = "name",
                example = "Project Omega")
        String name,
        @Schema(name = "description",
                example = "Omega description")
        String description,
        @Schema(name = "dueDate",
                example = "2026-01-01")
        LocalDate dueDate,
        @Schema(name = "projectId",
                example = "1")
        @Positive
        Long projectId,
        @Schema(name = "assigneeId",
                example = "1")
        @Positive
        Long assigneeId) {}

