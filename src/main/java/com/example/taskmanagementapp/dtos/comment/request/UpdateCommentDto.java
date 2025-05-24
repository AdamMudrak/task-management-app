package com.example.taskmanagementapp.dtos.comment.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateCommentDto(
        @Schema(name = "text",
                example = "This task should be done using...",
                requiredMode = REQUIRED)
        @NotBlank
        String text) {
}
