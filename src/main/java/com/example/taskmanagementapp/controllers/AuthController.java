package com.example.taskmanagementapp.controllers;

import static com.example.taskmanagementapp.constants.Constants.AUTHORIZATION_REQUIRED;
import static com.example.taskmanagementapp.constants.Constants.CODE_200;
import static com.example.taskmanagementapp.constants.Constants.CODE_201;
import static com.example.taskmanagementapp.constants.Constants.CODE_400;
import static com.example.taskmanagementapp.constants.Constants.INVALID_ENTITY_VALUE;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.AUTH_API_DESCRIPTION;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.AUTH_API_NAME;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.CHANGE_PASSWORD_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.EMAIL_LOGIN_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.INITIATE_PASSWORD_RESET_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.REGISTER_SUMMARY;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_CHANGE_PASSWORD;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_INITIATED_PASSWORD_RESET;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_LOGGED_IN;
import static com.example.taskmanagementapp.constants.controllers.AuthControllerConstants.SUCCESSFULLY_REGISTERED;

import com.example.taskmanagementapp.constants.Constants;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordResetLinkRequest;
import com.example.taskmanagementapp.dtos.authentication.request.UserLoginRequestDto;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.authentication.response.ChangePasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LinkToResetPasswordSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.LoginSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationSuccessDto;
import com.example.taskmanagementapp.dtos.authentication.response.SendLinkToResetPasswordDto;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.LoginException;
import com.example.taskmanagementapp.exceptions.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.RegistrationException;
import com.example.taskmanagementapp.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Tag(name = AUTH_API_NAME,
        description = AUTH_API_DESCRIPTION)
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final AuthenticationService authenticationService;

    @Operation(summary = REGISTER_SUMMARY)
    @ApiResponse(responseCode = CODE_201, description =
            SUCCESSFULLY_REGISTERED)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationSuccessDto register(
            @RequestBody @Valid UserRegistrationRequestDto requestDto)
                                            throws RegistrationException {
        return authenticationService.register(requestDto);
    }

    @Operation(hidden = true)
    @GetMapping("/register-success")
    public RegistrationConfirmationSuccessDto confirmRegistration(
            HttpServletRequest httpServletRequest) {
        return authenticationService
                    .confirmRegistration(httpServletRequest);
    }

    @Operation(summary = EMAIL_LOGIN_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_LOGGED_IN)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @PostMapping("/login")
    public LoginSuccessDto login(@RequestBody @Valid UserLoginRequestDto request,
                                 HttpServletResponse response) throws LoginException {
        return authenticationService.authenticateUser(request, response);
    }

    @Operation(summary = INITIATE_PASSWORD_RESET_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_INITIATED_PASSWORD_RESET)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @PostMapping("/forgot-password")
    public SendLinkToResetPasswordDto initiatePasswordReset(@RequestBody @Valid
                                                                PasswordResetLinkRequest request)
                                                                        throws LoginException {
        return authenticationService.sendPasswordResetLink(request.emailOrUsername());
    }

    @Operation(hidden = true)
    @GetMapping("/reset-password")
    public LinkToResetPasswordSuccessDto resetPassword(HttpServletRequest httpServletRequest) {
        return authenticationService
                .confirmResetPassword(httpServletRequest);
    }

    @Operation(summary = CHANGE_PASSWORD_SUMMARY)
    @ApiResponse(responseCode = CODE_200, description =
            SUCCESSFULLY_CHANGE_PASSWORD)
    @ApiResponse(responseCode = CODE_400, description = INVALID_ENTITY_VALUE)
    @ApiResponse(responseCode = Constants.CODE_401, description = AUTHORIZATION_REQUIRED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/change-password")
    public ChangePasswordSuccessDto changePassword(@AuthenticationPrincipal User user,
                                                   @RequestBody @Valid
                                                   PasswordChangeRequest request)
                                                    throws PasswordMismatchException {
        return authenticationService.changePassword(user, request);
    }
}
