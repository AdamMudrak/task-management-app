package com.example.taskmanagementapp.dtos.authentication.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.taskmanagementapp.constants.Constants;
import com.example.taskmanagementapp.validation.emailandusername.Email;
import com.example.taskmanagementapp.validation.emailandusername.NotLikeEmail;
import com.example.taskmanagementapp.validation.fieldmatch.FieldRegisterMatch;
import com.example.taskmanagementapp.validation.password.Password;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
@FieldRegisterMatch
public record RegistrationRequest(
        @Schema(name = "username",
                example = "ExampleUsername",
                requiredMode = REQUIRED)
        @NotBlank
        @NotLikeEmail
        String username,
        @Schema(name = "password",
                example = "Best_Password1@3$",
                description = Constants.PASSWORD_DESCRIPTION,
                requiredMode = REQUIRED)
        @NotBlank
        @Password
        String password,
        @Schema(name = "repeatPassword",
                example = "Best_Password1@3$",
                description = "This field must be the same as password!",
                requiredMode = REQUIRED)
        @NotBlank
        @Password
        String repeatPassword,
        @Schema(name = "email",
                example = "example@gmail.com",
                requiredMode = REQUIRED)
        @NotBlank
        @Email
        String email,
        @Schema(name = "firstName",
                example = "John",
                requiredMode = REQUIRED)
        @NotBlank
        String firstName,
        @Schema(name = "lastName",
                example = "Doe",
                requiredMode = REQUIRED)
        @NotBlank
        String lastName){}
