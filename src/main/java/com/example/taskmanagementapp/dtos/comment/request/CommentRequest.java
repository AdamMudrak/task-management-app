package com.example.taskmanagementapp.dtos.comment.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentRequest(
        @Schema(name = "taskId",
                example = "1",
                requiredMode = REQUIRED)
        @Positive
        @NotNull
        Long taskId,
        @NotBlank
        @Schema(name = "text",
                example = "This task should be done using...",
                requiredMode = REQUIRED)
        String text) {}
