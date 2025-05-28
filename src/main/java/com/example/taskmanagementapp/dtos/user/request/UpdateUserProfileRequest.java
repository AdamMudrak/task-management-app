package com.example.taskmanagementapp.dtos.user.request;

import com.example.taskmanagementapp.validation.emailandusername.Email;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateUserProfileRequest(
        @Schema(name = "firstName",
                example = "John")
        String firstName,
        @Schema(name = "lastName",
                example = "Doe")
        String lastName,
        @Schema(name = "email",
                example = "example@gmail.com")
        @Email
        String email) {
}
