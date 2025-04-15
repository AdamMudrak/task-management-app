package com.example.taskmanagementapp.dtos.authentication.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.UserDtoConstants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GetLinkToResetPasswordDto(
        @Schema(name = UserDtoConstants.EMAIL_OR_USERNAME,
                example = UserDtoConstants.EMAIL_OR_USERNAME_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        String emailOrUsername){}
