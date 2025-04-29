package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.ROLE_EMPLOYEE;
import static com.example.taskmanagementapp.constants.Constants.ROLE_MANAGER;
import static com.example.taskmanagementapp.constants.Constants.ROLE_SUPERVISOR;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.CHANGE_EMAIL_CONFIRMATION;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.CHANGE_EMAIL_SUCCESS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.GET_PROFILE_INFO;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.GET_PROFILE_INFO_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.RETRIEVE_ALL_USERS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_CHANGED_EMAIL;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_RETRIEVED;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_RETRIEVE_ALL_USERS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_UPDATED_PROFILE_INFO;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_UPDATED_ROLE;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.UPDATE_PROFILE_INFO;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.UPDATE_PROFILE_INFO_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.UPDATE_USER_ROLE;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.UPDATE_USER_ROLE_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.USERS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.USER_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.USER_API_NAME;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDtoOnUpdate;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(USERS)
@Tag(name = USER_API_NAME,
        description = USER_API_DESCRIPTION)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = GET_PROFILE_INFO_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_RETRIEVED)
    @GetMapping(GET_PROFILE_INFO)
    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    UserProfileInfoDto getProfileInfo(@AuthenticationPrincipal User user) {
        return userService.getProfileInfo(user.getId());
    }

    @Operation(summary = UPDATE_PROFILE_INFO_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_PROFILE_INFO)
    @PutMapping(UPDATE_PROFILE_INFO)
    @PreAuthorize(ROLE_EMPLOYEE + " or "
            + ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    UserProfileInfoDtoOnUpdate updateProfileInfo(@AuthenticationPrincipal User user,
                                                 UpdateUserProfileDto updateUserProfileDto) {
        return userService.updateProfileInfo(user.getId(), updateUserProfileDto);
    }

    @Operation(summary = UPDATE_USER_ROLE_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_ROLE)
    @PutMapping(UPDATE_USER_ROLE)
    @PreAuthorize(ROLE_SUPERVISOR)
    UserProfileInfoDto updateUserRole(@AuthenticationPrincipal User user,
                                      @Positive @PathVariable Long employeeId,
                                      RoleNameDto roleName) {
        return userService.updateUserRole(user.getId(), employeeId, roleName);
    }

    @Operation(summary = RETRIEVE_ALL_USERS)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_RETRIEVE_ALL_USERS)
    @PreAuthorize(ROLE_MANAGER + " or "
            + ROLE_SUPERVISOR)
    @GetMapping
    List<UserProfileInfoDto> getAllUsers(@Parameter(example = PAGEABLE_EXAMPLE) Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @Operation(summary = CHANGE_EMAIL_CONFIRMATION, hidden = true)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_CHANGED_EMAIL)
    @GetMapping(CHANGE_EMAIL_SUCCESS)
    UserProfileInfoDto changeEmailSuccess(HttpServletRequest request) {
        return userService.confirmEmailChange(request);
    }
}
