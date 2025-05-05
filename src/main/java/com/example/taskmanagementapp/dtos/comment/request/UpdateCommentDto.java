package com.example.taskmanagementapp.dtos.comment.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.CommentDtoConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateCommentDto(
        @Schema(name = CommentDtoConstants.TEXT,
                example = CommentDtoConstants.TEXT_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        String text) {
}
