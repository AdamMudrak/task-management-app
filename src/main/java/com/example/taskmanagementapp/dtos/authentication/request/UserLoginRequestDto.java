package com.example.taskmanagementapp.dtos.authentication.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.UserDtoConstants;
import com.example.taskmanagementapp.validation.email.Email;
import com.example.taskmanagementapp.validation.password.Password;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserLoginRequestDto(
        @Schema(name = UserDtoConstants.EMAIL_OR_USERNAME,
        example = UserDtoConstants.EMAIL_OR_USERNAME_EXAMPLE,
        requiredMode = REQUIRED)
        @NotBlank
        @Email
        String emailOrUsername,

        @Schema(name = UserDtoConstants.PASSWORD,
        example = UserDtoConstants.PASSWORD_EXAMPLE,
        description = UserDtoConstants.PASSWORD_DESCRIPTION,
        requiredMode = REQUIRED)
        @NotBlank
        @Password
        String password) {}
