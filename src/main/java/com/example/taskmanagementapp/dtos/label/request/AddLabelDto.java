package com.example.taskmanagementapp.dtos.label.request;

import static com.example.taskmanagementapp.constants.dtos.LabelDtoConstants.NAME;
import static com.example.taskmanagementapp.constants.dtos.LabelDtoConstants.NAME_EXAMPLE;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AddLabelDto(
        @Schema(name = NAME, example = NAME_EXAMPLE,
        requiredMode = REQUIRED)
        @NotBlank
        String name){}
