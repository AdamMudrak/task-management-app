package com.example.taskmanagementapp.dtos.label.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LabelRequest(
        @Schema(name = "name", example = "Best label",
        requiredMode = REQUIRED)
        @NotBlank
        String name){}
