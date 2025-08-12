package com.example.taskmanagementapp.dto.label.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateLabelRequest(
        @Schema(name = "name",
                example = "Best label")
        String name
){}
