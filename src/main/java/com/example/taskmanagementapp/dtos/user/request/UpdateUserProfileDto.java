package com.example.taskmanagementapp.dtos.user.request;

import com.example.taskmanagementapp.constants.dtos.UserDtoConstants;
import com.example.taskmanagementapp.validation.emailandusername.Email;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateUserProfileDto(
        @Schema(name = UserDtoConstants.FIRST_NAME,
                example = UserDtoConstants.FIRST_NAME_EXAMPLE)
        String firstName,
        @Schema(name = UserDtoConstants.LAST_NAME,
                example = UserDtoConstants.LAST_NAME_EXAMPLE)
        String lastName,
        @Schema(name = UserDtoConstants.EMAIL,
                example = UserDtoConstants.EMAIL_EXAMPLE)
        @Email
        String email) {
}
