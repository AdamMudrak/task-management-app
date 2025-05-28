package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.CHANGE_USER_ACCOUNT_STATUS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.GET_PROFILE_INFO_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.PAGEABLE_EXAMPLE;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.RETRIEVE_ALL_USERS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_CHANGED_STATUS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_RETRIEVED;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_RETRIEVE_ALL_USERS;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_UPDATED_PROFILE_INFO;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.SUCCESSFULLY_UPDATED_ROLE;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.UPDATE_PROFILE_INFO_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.UPDATE_USER_ROLE_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.USER_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.UserControllerConstants.USER_API_NAME;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileRequest;
import com.example.taskmanagementapp.dtos.user.request.UserAccountStatusDto;
import com.example.taskmanagementapp.dtos.user.response.UpdateUserProfileResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = USER_API_NAME,
        description = USER_API_DESCRIPTION)
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @Operation(summary = GET_PROFILE_INFO_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_RETRIEVED)
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UserProfileResponse getProfileInfo(@AuthenticationPrincipal User user) {
        return userService.getProfileInfo(user.getId());
    }

    @Operation(summary = UPDATE_PROFILE_INFO_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_PROFILE_INFO)
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UpdateUserProfileResponse updateProfileInfo(@AuthenticationPrincipal User user,
                                                       @RequestBody @Valid
                                                 UpdateUserProfileRequest updateUserProfileDto) {
        return userService.updateProfileInfo(user.getId(), updateUserProfileDto);
    }

    @Operation(summary = UPDATE_USER_ROLE_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_UPDATED_ROLE)
    @PutMapping("/{employeeId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse updateUserRole(@AuthenticationPrincipal User user,
                                              @PathVariable @Positive Long employeeId,
                                              @RequestParam RoleNameDto roleName)
            throws ForbiddenException {
        return userService.updateUserRole(user.getId(), employeeId, roleName);
    }

    @Operation(summary = RETRIEVE_ALL_USERS)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_RETRIEVE_ALL_USERS)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserProfileResponse> getAllUsers(
            @Parameter(example = PAGEABLE_EXAMPLE) Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @Operation(summary = CHANGE_USER_ACCOUNT_STATUS)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_CHANGED_STATUS)
    @PostMapping("/change-user-account-status/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserProfileResponse changeUserAccountStatus(@AuthenticationPrincipal User user,
                                   @RequestParam UserAccountStatusDto accountStatusDto,
                                   @PathVariable @Positive Long userId) throws ForbiddenException {
        return userService.changeStatus(user, userId, accountStatusDto);
    }

    @Operation(hidden = true)
    @GetMapping("/change-email-success")
    public UserProfileResponse changeEmailSuccess(HttpServletRequest request) {
        return userService.confirmEmailChange(request);
    }
}
