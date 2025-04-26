package com.example.taskmanagementapp.dtos.authentication.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.UserDtoConstants;
import com.example.taskmanagementapp.validation.fieldmatch.FieldCurrentAndNewPasswordCollision;
import com.example.taskmanagementapp.validation.fieldmatch.FieldSetNewPasswordMatch;
import com.example.taskmanagementapp.validation.password.Password;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@FieldSetNewPasswordMatch
@FieldCurrentAndNewPasswordCollision
public record SetNewPasswordDto(
        @Schema(name = UserDtoConstants.CURRENT_PASSWORD,
                example = UserDtoConstants.PASSWORD_EXAMPLE,
                description = UserDtoConstants.PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @NotBlank
        String currentPassword,
        @Schema(name = UserDtoConstants.NEW_PASSWORD,
                example = UserDtoConstants.PASSWORD_EXAMPLE,
                description = UserDtoConstants.PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @Size(min = UserDtoConstants.MIN_PASSWORD_SIZE,
                max = UserDtoConstants.MAX_PASSWORD_SIZE)
        @NotBlank
        @Password
        String newPassword,
        @Schema(name = UserDtoConstants.REPEAT_NEW_PASSWORD,
                example = UserDtoConstants.PASSWORD_EXAMPLE,
                description = UserDtoConstants.REPEAT_PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @NotBlank
        String repeatNewPassword){}
