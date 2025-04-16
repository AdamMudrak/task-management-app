package com.example.taskmanagementapp.dtos.authentication.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.dtos.UserDtoConstants;
import com.example.taskmanagementapp.validation.email.Email;
import com.example.taskmanagementapp.validation.fieldmatch.FieldRegisterMatch;
import com.example.taskmanagementapp.validation.password.Password;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@FieldRegisterMatch
public record UserRegistrationRequestDto(
        @Schema(name = UserDtoConstants.USERNAME,
                example = UserDtoConstants.USERNAME_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        String username,

        @Schema(name = UserDtoConstants.PASSWORD,
                example = UserDtoConstants.PASSWORD_EXAMPLE,
                description = UserDtoConstants.PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @Size(min = UserDtoConstants.MIN_PASSWORD_SIZE,
                max = UserDtoConstants.MAX_PASSWORD_SIZE)
        @NotBlank
        @Password String password,

        @Schema(name = UserDtoConstants.REPEAT_PASSWORD,
                example = UserDtoConstants.PASSWORD_EXAMPLE,
                description = UserDtoConstants.REPEAT_PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @NotBlank
        @Password String repeatPassword,

        @Schema(name = UserDtoConstants.EMAIL,
                example = UserDtoConstants.EMAIL_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        @Email String email,

        @Schema(name = UserDtoConstants.FIRST_NAME,
                example = UserDtoConstants.FIRST_NAME_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        String firstName,

        @Schema(name = UserDtoConstants.LAST_NAME,
                example = UserDtoConstants.LAST_NAME_EXAMPLE,
                requiredMode = REQUIRED)
        @NotBlank
        String lastName){}
