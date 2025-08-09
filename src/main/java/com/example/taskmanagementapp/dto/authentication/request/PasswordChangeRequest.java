package com.example.taskmanagementapp.dto.authentication.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constant.Constants;
import com.example.taskmanagementapp.validation.fieldmatch.FieldCurrentAndNewPasswordCollision;
import com.example.taskmanagementapp.validation.fieldmatch.FieldSetNewPasswordMatch;
import com.example.taskmanagementapp.validation.password.Password;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@FieldSetNewPasswordMatch
@FieldCurrentAndNewPasswordCollision
public record PasswordChangeRequest(
        @Schema(name = "currentPassword",
                example = "Best_Password1@3$",
                description = Constants.PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @NotBlank
        @Password
        String currentPassword,
        @Schema(name = "newPassword",
                example = "Best_Password1@3$",
                description = Constants.PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @NotBlank
        @Password
        String newPassword,
        @Schema(name = "repeatNewPassword",
                example = "Best_Password1@3$",
                description = "This field must be the same as password!",
                requiredMode = REQUIRED)
        @NotBlank
        @Password
        String repeatNewPassword){}
