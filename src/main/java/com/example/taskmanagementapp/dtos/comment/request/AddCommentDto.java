package com.example.taskmanagementapp.dtos.comment.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.CommentDtoConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddCommentDto(
        @Schema(name = CommentDtoConstants.TASK_ID,
                example = CommentDtoConstants.TASK_ID_EXAMPLE,
                requiredMode = REQUIRED)
        @Positive
        @NotNull
        Long taskId,
        @NotBlank
        @Schema(name = CommentDtoConstants.TEXT,
                example = CommentDtoConstants.TEXT_EXAMPLE,
                requiredMode = REQUIRED)
        String text) {}
