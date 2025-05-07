package com.example.taskmanagementapp.dtos.label.request;

import static com.example.taskmanagementapp.constants.dtos.LabelDtoConstants.NAME;
import static com.example.taskmanagementapp.constants.dtos.LabelDtoConstants.NAME_EXAMPLE;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateLabelDto(
        @Schema(name = NAME, example = NAME_EXAMPLE)
        String name
){}
