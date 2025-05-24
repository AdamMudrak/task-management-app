package com.example.taskmanagementapp.dtos.project.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.validation.date.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectRequest(
        @Schema(name = "name",
                example = "Best Project",
                requiredMode = REQUIRED)
        @NotBlank
        String name,
        @Schema(name = "description",
                example = "Project for best company")
        String description,
        @Schema(name = "startDate",
                example = "2025-01-01",
                requiredMode = REQUIRED)
        @NotNull
        @Date
        LocalDate startDate,
        @Schema(name = "endDate",
                example = "2025-12-31",
                requiredMode = REQUIRED)
        @NotNull
        @Date
        LocalDate endDate) {}
